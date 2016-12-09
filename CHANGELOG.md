# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.1.0] - 2016-12-099 - _Navajo_
### Added
 - Added Vault Integration, which replaces the outdated KeyServer

### Fixed
 - [[TAPTKR-5](http://morden.sdsu.edu:9000/issue/TAPTKR-5)] Replacing the Key
 Server with Vault fixes this issue, as KeyServer would throw null pointers
 when it got grumpy.

### Changed
 - Updated Documentation for Vault and its configuration.
 - Renamed `RELEASES.md` to `CHANGELOG.md` which will include all notable
 changes and will follow the format specified by [Keep a Changelog](http://keepachangelog.com/).

### Removed
 - Key Server Support is no longer included, and the `ks_path` and `ks_key`
 environment variables can be removed from your configuration.

## [1.0.0] - 2016-07-05 - _Carolina_
### Added
 - Initial stable release of Tap Tracker.
 - There are still a few bugs and features that will be added in the near
 future, but this version is safe to use and should be stable.
