# UTMStack Datasource for AS400
## Description
UTMStack Datasource for AS400 is a tool developed in Java (using JDK 11), to interact with IBM i Systems.
The main function is to extract logs from IBM i System and send them to syslog server.

## Usage
In order to extract logs using this tool, you must configure some environment variables 

- _SYSLOG_PROTOCOL_: Represents the protocol of the listening syslog server to send logs to. Example: `udp`.
- _SYSLOG_HOST_: Represents the IP or HOST of the listening syslog server to send logs to. Example: `172.20.0.18`.
- _SYSLOG_PORT_: Represents the port of the listening syslog server to send logs to. Example: `514`.
- _AS400_HOST_NAME_: Represents  the hostname of the IBM i System, where the logs will be extracted. Example: `PUB400.COM`.
- _AS400_USER_ID_: Represents the user id used to connect to the IBM i System. Example: `USER1`.
- _AS400_USER_PASSWORD_: Represents the user's password to connect to IBM System. Example: `mypassword1`.

To execute the tool, you must create a docker container using this docker image `docker pull ghcr.io/atlasinsidecorp/as400jds:prod_${time_mark}`, 
with the environment variables configured as described before.
Example of code before, with ${time_mark} set:
`docker pull ghcr.io/atlasinsidecorp/as400jds:prod_20230120030016`. Contact to the repository administrator to get the latest version.