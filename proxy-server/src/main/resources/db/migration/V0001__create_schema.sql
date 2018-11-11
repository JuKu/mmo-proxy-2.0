--
-- Tabellenstruktur für Tabelle `${prefix}users`
--

CREATE TABLE IF NOT EXISTS `${prefix}proxy_users` (
  `userID` int(10) NOT NULL,
  `username` varchar(255) NOT NULL,
  `ip` varchar(255) NOT NULL,
  `online` int(10) NOT NULL DEFAULT '0',
  `last_online` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `activated` int(10) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `${prefix}users`
--
ALTER TABLE `${prefix}proxy_users`
 ADD PRIMARY KEY (`userID`), ADD UNIQUE KEY `username` (`username`), ADD KEY `online` (`online`,`last_online`);

--
-- AUTO_INCREMENT für exportierte Tabellen
--

--
-- AUTO_INCREMENT für Tabelle `${prefix}users`
--
ALTER TABLE `${prefix}proxy_users`
MODIFY `userID` int(10) NOT NULL AUTO_INCREMENT;

-- characters of player
CREATE TABLE IF NOT EXISTS `${prefix}proxy_characters` (
  `cid` int(10) NOT NULL,
  `userID` int(10) NOT NULL DEFAULT '-1',
  `name` varchar(255) NOT NULL,
  `data` text NOT NULL,
  `current_regionID` int(10) NOT NULL DEFAULT '1',
  `instanceID` int(10) NOT NULL DEFAULT '1',
  `shardID` int(10) NOT NULL DEFAULT '1',
  `activated` int(10) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Indizes für die Tabelle `${prefix}characters`
--
ALTER TABLE `${prefix}proxy_characters`
 ADD PRIMARY KEY (`cid`), ADD KEY `userID` (`userID`), ADD KEY `current_regionID` (`current_regionID`);

--
-- AUTO_INCREMENT für Tabelle `${prefix}characters`
--
ALTER TABLE `${prefix}proxy_characters`
MODIFY `cid` int(10) NOT NULL AUTO_INCREMENT;
