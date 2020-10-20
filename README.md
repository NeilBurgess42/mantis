# Mantis

Ethereum-like Blockchain Scala client built by IOHK's Team Grothendieck.

### Status

Continuous Integration Build Status [FIXME]

Unit Test Code Coverage Status [FIXME]

// FIXME: Should we continue using this? or should we migrate to atlassian wiki?
For more details on configuration and functionality check out our [wiki](http://mantis.readthedocs.io) (also at [wiki](https://github.com/input-output-hk/mantis/wiki))

### Download the client

The latest release can be downloaded from [here](https://github.com/input-output-hk/mantis/releases)

### Command line version

You can use generic launcher with appropriate parameter to connect with pre-configured network, it can be found in `bin` directory.

Example:
  - `./bin/mantis-launcher etc` - for joining Ethereum Classic network

Possible networks: `etc`, `eth`, `mordor`, `testnet-internal`

### Command Line Interface

`mantis-cli` is a tool that can be used to:
 
 - generate a new private key
 ```
./bin/mantis mantis-cli generate-private-key
```
 - derive an address from private key
```
./bin/mantis mantis-cli derive-address 00b11c32957057651d56cd83085ef3b259319057e0e887bd0fdaee657e6f75d0
```
 - generate genesis allocs (using private keys or addresses)
```
(private keys) `./bin/mantis mantis-cli generate-alloc --balance=42 00b11c32957057651d56cd83085ef3b259319057e0e887bd0fdaee657e6f75d0 00b11c32957057651d56cd83085ef3b259319057e0e887bd0fdaee657e6f75d1`
(addresses) `./bin/mantis mantis-cli generate-alloc --balance=42 --useAddresses 8b196738d90cf3d9fc299e0ec28e15ebdcbb0bdcb281d9d5084182c9c66d5d12`
```

### Building the client

#### SBT

##### Prerequisites to build

- JDK 1.8 (download from [java.com](http://www.java.com))
- sbt ([download sbt](http://www.scala-sbt.org/download.html))
- python 2.7.15 (download from [python.org](https://www.python.org/downloads/))

##### Build the client

As an alternative to downloading the client build the client from source.


```
git submodule update --recursive --init
sbt dist
```

in the root of the project.

This updates all submodules and creates a distribution zip in `~/target/universal/`.

#### Nix

In the root of the project:

##### Build the client

```
nix-build
```

##### Regenerate lock files

```
nix-shell
sbtix-gen-all2
```

OR

If the "ensure Nix expressions are up-to-date" step of your CI
build has failed, check the artifacts of that step. There should be a
patch provided, which you can apply locally with:

```
patch -p1 < downloaded.patch
```

This patch will update the lock files for you.

###### Why so many lock files?

- `repo.nix`                 : generated by the `sbtix-gen` command and includes only the build dependencies for the project.
- `project/repo.nix`         : generated by the `sbtix-gen-all` command and includes only the plugin dependencies. Also generates `repo.nix`.
- `project/project/repo.nix` : generated by the `sbtix-gen-all2` command and includes only the plugin dependencies. Also generates `repo.nix` and `project/repo.nix`.

##### error: unsupported argument 'submodules' to 'fetchGit'

You get this error when you aren't using a new-enough version of Nix (fetchGit support for submodules is recent).

To fix this, update the version of Nix you are using, or in a pinch:

  - Remove the "submodules = true;" argument from fetchGit (in `./nix/pkgs/mantis/default.nix`).
  - `git submodule update --recursive --init`
  - `nix-build`

### Monitoring

#### Locally build & run monitoring client

```
# Build monitoring client docker image
projectRoot $ docker build -f ./docker/monitoring-client.Dockerfile -t mantis-monitoring-client ./docker/
# Run monitoring client in http://localhost:9090
projectRoot $ docker run --network=host mantis-monitoring-client
```

### Feedback

Feedback gratefully received through the Ethereum Classic Forum (http://forum.ethereumclassic.org/)

### Known Issues

There is a list of known issues in the 'RELEASE' file located in the root of the installation.

