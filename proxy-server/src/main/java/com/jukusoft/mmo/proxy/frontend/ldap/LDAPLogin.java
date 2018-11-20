package com.jukusoft.mmo.proxy.frontend.ldap;

import com.jukusoft.mmo.engine.shared.config.Config;
import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.proxy.frontend.database.Database;
import com.jukusoft.mmo.proxy.frontend.login.User;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class LDAPLogin {

    //https://cweiske.de/tagebuch/ldap-server-travis.htm

    //https://www.hascode.com/2016/07/ldap-testing-with-java-apacheds-vs-embedded-ldap-junit/

    protected String host = "";
    protected int port = 389;
    protected boolean ssl = false;

    protected String userPrefix = "";
    protected String userSuffix = "";

    protected static final String INSERT_QUERY = String.format("INSERT INTO `mmo_proxy_users` (   `userID`, `username`, `ip`, `online`, `last_online`, `activated`) VALUES (   NULL, ?, ?, '1', CURRENT_TIMESTAMP, '1') ON DUPLICATE KEY UPDATE `ip` = ?, `online` = '1', `last_online` = NOW();");
    protected static final String SELECT_QUERY = String.format("SELECT * FROM `mmo_proxy_users` WHERE `username` = ?; ");

    public static final String LOG_TAG = "LDAPLogin";

    public LDAPLogin() {
        this.host = Config.get("LDAP", "host");
        this.port = Config.getInt("LDAP", "port");
        this.ssl = Config.getBool("LDAP", "ssl");
        this.userPrefix = Config.get("LDAP", "user_prefix");
        this.userSuffix = Config.get("LDAP", "user_suffix");
    }

    public User login(String username, String password, String ip) {
        long startTime = System.currentTimeMillis();

        // setup the environment
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, String.format("ldap://%s:%s", this.host, this.port));
        env.put("com.sun.jndi.ldap.connect.pool", "true");

        if (this.ssl) {
            //activate ssl
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }

        //generate userDN
        String userDn = this.userPrefix + username.replace(",", "") + this.userSuffix;
        Log.i(LOG_TAG, "ldap server: " + host + ":" + port);
        Log.i(LOG_TAG, "try to login ldap user: " + userDn);

        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDn);//example: "cn=S. User, ou=NewHires, o=JNDITutorial"
        env.put(Context.SECURITY_CREDENTIALS, password);
        DirContext context = null;

        // do the ldap bind (validate username and password
        boolean loggedIn;
        try {
            context = new InitialDirContext(env);
            loggedIn = true;
        } catch (NamingException e) {
            loggedIn = false;
            return null;
        }

        Log.i(LOG_TAG, "authorization successful for user '" + userDn + "'!");

        //get groups of user for permissions
        List<String> groups = listGroups(context, Config.get("LDAP", "users_container"), username);

        try (Connection conn = Database.getConnection()) {
            Log.v(LOG_TAG, "execute sql query: " + INSERT_QUERY);

            try (PreparedStatement stmt = conn.prepareStatement(INSERT_QUERY)) {
                //insert user, if absent
                stmt.setString(1, username);
                stmt.setString(2, ip);
                stmt.setString(3, ip);
                stmt.execute();
            }

            try (PreparedStatement stmt = conn.prepareStatement(SELECT_QUERY)) {
                //get userID
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        //get first element
                        int userID = rs.getInt("userID");
                        int activated = rs.getInt("activated");

                        //check, if user is activated
                        if (activated != 1) {
                            Log.w(LOG_TAG, "user '" + username + "' exists but is not activated.");
                            return null;
                        }

                        long endTime = System.currentTimeMillis();
                        long diffTime = endTime - startTime;
                        Log.v(LOG_TAG, "login takes " + diffTime + "ms");

                        //create and return new user object
                        return new User(userID, username, groups);
                    }
                }
            }
        } catch (SQLException e) {
            Log.w(LOG_TAG, "SQLException while login: ", e);
            return null;
        }

        return null;
    }

    protected List<String> listGroups (DirContext ctx, String usersContainer, String username) {
        //https://stackoverflow.com/questions/34491680/get-groups-using-ldap-in-java

        List<String> groups = new ArrayList<>();

        String[] attrIDs = {"cn"};
        SearchControls ctls = new SearchControls();
        ctls.setReturningAttributes(attrIDs);
        String[] attributes = {"memberOf", "member"};
        //ctls.setReturningAttributes(attributes);
        //ctls.setReturningAttributes(new String[]{"*", "+"});

        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        try {
            NamingEnumeration<SearchResult> answer = ctx.search(usersContainer, "(|(memberUid=" + username.toLowerCase() + ")(&(uid=" + username.toLowerCase() + ")(memberOf=*)))", ctls);//(sAMAccountName=userName), (&(|(objectclass=user)(objectclass=inetOrgPerson))(uid=" + username + "))

            int k = 0;

            while (answer.hasMore()) {
                Log.v(LOG_TAG, "user object found in ldap search for user '" + username + "'.");

                SearchResult sr = answer.next();
                Log.v(LOG_TAG, "group membership found: " + sr.toString() + " with " + sr.getAttributes().size() + " attributes.");

                /*NamingEnumeration<String> namingEnumeration = sr.getAttributes().getIDs();

                while (namingEnumeration.hasMore()) {
                    String attrKey = namingEnumeration.next();
                    Attribute attribute = sr.getAttributes().get(attrKey);

                    System.err.println("attribute found: " + attribute.get());
                }

                NamingEnumeration<? extends Attribute> enumeration = sr.getAttributes().getAll();*/

                /*while (enumeration.hasMore()) {
                    Attribute attr = enumeration.next();
                    System.err.println("attr: " + attr.toString());
                }*/

                Attribute groupCn = sr.getAttributes().get("cn");
                String cn = (String) groupCn.get();

                Log.v(LOG_TAG, "[user: " + username + "] ldap group membership found: " + cn);

                groups.add(cn);

                k++;
            }

            if (k == 0) {
                Log.w(LOG_TAG, "LDAP: no user object found in ldap search for user '" + username + "'!");
            }
        } catch (NamingException e) {
            Log.w(LOG_TAG, "NamingException while getting groups from ldap: ", e);
        }

        if (groups.isEmpty()) {
            Log.w(LOG_TAG, "no groups found for user '" + username + "'!");
        }

        return groups;
    }

}
