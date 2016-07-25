Tap Tracker Server
==================

Tap Tracker allows IoT devices (Like the [Tap Tracker Client](https://bitbucket.org/sdsu-its/tap-tracker-arduino))
to log event data. The Admin area of Tap Tracker allows device statistics to be analyzed and for devices and users
to be managed, all from within the WebUI.

## SetUp
If you plan on setting up a staging environment, you will need to repeat the DB Configuration
and the KeyServer Setup, choosing another unique app name, which you well set for the respective
deployments. It is not possible to have a staging and production environment running on the same
Tomcat server, because of environment variable overlap.

Once you have configured the Key Server and the Database, on first run, the Web App will create an
admin account with with you can log in. Once logged in, it is recommended that you change the default
password. You can also create new users and devices at this point.

**Username:** `admin` <br>
**Password:** `admin`

### DB Config
You will need to setup a variety of tables all in one MySQL DB. Tap Tracker is designed
to be flexible in regard to database location and other tables in the Database.

#### Table Breakdown
**Devices** - Saves the client devices and their names. Due to foreign key restrictions,
a device must exist with the corresponding ID for an event to be created for that device.

**Events** - Logs events, the timestamp is set by default to the current time, but can
be overwritten if necessary.

**Users** - Saves users and their respective passwords. Usernames are restricted to 32
characters, but that can be adjusted in the DDL below.

#### DDL
```
CREATE TABLE devices (
  `id`   INTEGER PRIMARY KEY NOT NULL,
  `name` TEXT
);
CREATE TABLE events (
  `id`        INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  `device_id` INTEGER,
  `type`      INT(1),
  `time`      TIMESTAMP DEFAULT NOW(),
  FOREIGN KEY (`device_id`) REFERENCES devices (`id`)
);
CREATE TABLE users (
  `username` VARCHAR(32) PRIMARY KEY NOT NULL,
  `password` TEXT
);
```

### KeyServer
The name of the app that you want to use needs to be set as the `WELCOME_APP`
environment variable. You will also need to set the `KSPATH` and `KSKEY`
environment variables to their corresponding values.

Production and Staging all share the same parameters.
- `db-password` - DB Password
- `db-url` - * jdbc:mysql://db_host:3306/db_name replace db_host, db_name and possibly the port with your MySQL server info*
- `db-user` - DB Username
- `project_token` - Unique Project Identifier for Session Tokens, If changed, all tokens will become invalid.
- `token_cypher` - Token Encryption Cypher. If changed, all tokens will become invalid.
- `token_ttl` - Token Longevity (How long will a user stay logged in)
