# Phsyberdome Dependency Scanner And License Detector 

A command line tool written in Java to scan the dependencies of a project and detect their Licenses. I wrote this while building an SCA Tool.

The tool scans the metadata file (e.g. pom.xml for maven based projects and package.json for npm) and creates a dependency tree by recursively resolving each dependency from respective repositories. The license (if found) is analyzed and matched with the licenses in the SPDX License Database using methods of NLP. The motivation for the algorithm of license detection is taken from [Link](https://github.com/go-enry/go-license-detector).

![SAMPLE_IMAGE](./images/npm-scan-result.jpg)

## TODO

### Features

- [X] License Detection through License files
- [ ] License Detection through README files (If no License file is found in the package)
- [X] Supports scanning remote public repo
- [ ] Export report as XML/JSON

### Supported package/project managers

- [X] NPM
- [X] JAVA Maven
- [ ] JAVA Gradle
- [ ] PyPI
- [ ] Rubygems
- [ ] Cargo
- [ ] Go Packages

### Misc

- [ ] Write Tests


## Build

Build the jar package
```
mvn -DskipTests package
```

## Usage

To scan a local project
`java -jar <path-to-jar> scan -src <path-for-the-project-to-scan>`

To scan a remote repository/package (zip/tarball)
`java -jar <path-to-jar> monitor -src <path-for-the-project-to-scan>`
