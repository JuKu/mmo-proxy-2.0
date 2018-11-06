# mmo-proxy-2.0

Another try to build a mmo proxy server

Previous version: [mmo-proxy-server](https://github.com/JuKu/mmo-proxy-server)

## Server Architecture

![Server Architecture](./docs/images/server_architecture.png)

## Changes to previous version

The previous version was partly over-engineered with many modules, now there is only one maven module again.
Also the network protocol & types has improved a little bit, because there was some things, they was difficult to implement.
The serialization method was completely reworked and improved to maintain the server easier. Also we introduced a new pileline model.
Additionally we have removed the "cid" field in network protocol, because it was completely redundant, every proxy - game server connection is for only one client on one region (we don't use multiplexing, because this causes new problems and isn't easy to perform very well).
And proxy isn't longer responsible for chat messages, anymore.

## General Aspects

  - proxy server should be mostly transparent
  - pipeline model with filters for security
  - proxy is responsible for authentification (login & registration)
  
## Flowchart

From **client side**:\
\
![Flowchart](./docs/images/Flowchart.png)