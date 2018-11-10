--
-- Tabellenstruktur für Tabelle `${prefix}users`
--

CREATE TABLE IF NOT EXISTS `${prefix}users` (
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
ALTER TABLE `${prefix}users`
 ADD PRIMARY KEY (`userID`), ADD UNIQUE KEY `username` (`username`), ADD KEY `online` (`online`,`last_online`);

--
-- AUTO_INCREMENT für exportierte Tabellen
--

--
-- AUTO_INCREMENT für Tabelle `${prefix}users`
--
ALTER TABLE `${prefix}users`
MODIFY `userID` int(10) NOT NULL AUTO_INCREMENT;