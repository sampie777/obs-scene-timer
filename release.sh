#!/bin/bash

APP_NAME="obs-scene-timer"
progname=$(basename $0)

function usage {
  cat << HEREDOC

     Usage: $progname [command]

     commands:
       patch                Release a patch version (0.0.X)
       minor                Release a minor version (0.X.0)
       major                Release a major version (X.0.0)
       -h, --help           Show this help message and exit

HEREDOC
}

function releasePatch {
  mvn test || exit 1
  git checkout master || exit 1

  # Create patch version
  CURRENT_VERSION=$(cat pom.xml | grep "<version>.*</version>" | head -1 |awk -F'[><]' '{print $3}')
  RELEASE_VERSION=$(echo ${CURRENT_VERSION} | awk -F'.' '{print $1"."$2"."$3+1}')

  git merge develop || exit 1

  mvn -q versions:set -DnewVersion="${RELEASE_VERSION}" -DgenerateBackupPoms=false || exit 1

  pushAndRelease
}

function releaseMinor {
  mvn test || exit 1
  git checkout master || exit 1
  git merge develop || exit 1

  mvn -q versions:set -DremoveSnapshot -DgenerateBackupPoms=false || exit 1

  pushAndRelease
}

function releaseMajor {
  mvn test || exit 1
  git checkout master || exit 1
  git merge develop || exit 1

  mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.nextMajorVersion}.0.0 -DgenerateBackupPoms=false versions:commit || exit 1

  pushAndRelease
}

function pushAndRelease {
  RELEASE_VERSION=$(cat pom.xml | grep "<version>.*</version>" | head -1 |awk -F'[><]' '{print $3}')
  echo "Release version: ${RELEASE_VERSION}"

  mvn clean install || exit 1
  chmod a+x "./target/${APP_NAME}-${RELEASE_VERSION}.exe" || exit 1
  chmod a+x "./target/${APP_NAME}-${RELEASE_VERSION}.jar" || exit 1

  git add pom.xml || exit 1
  git commit -m "version release: ${RELEASE_VERSION}" || exit 1
  git tag "v${RELEASE_VERSION}" || exit 1
  git push -u origin master --tags || exit 1
}

function setNextDevelopmentVersion {
  git checkout develop || exit 1
  git rebase master || exit 1

  # Generate next (minor) development version
  RELEASE_VERSION=$(cat pom.xml | grep "<version>.*</version>" | head -1 |awk -F'[><]' '{print $3}')
  DEV_VERSION=$(echo ${RELEASE_VERSION} | awk -F'.' '{print $1"."$2+1".0-SNAPSHOT"}')

  echo "Next development version: ${DEV_VERSION}"
  mvn -q versions:set -DnewVersion="${DEV_VERSION}" -DgenerateBackupPoms=false || exit 1

  git add pom.xml || exit 1
  git commit -m "next development version" || exit 1
  git push -u origin develop --tags || exit 1
}

command="$1"
case $command in
  patch)
    releasePatch
    setNextDevelopmentVersion
    ;;
  minor)
    releaseMinor
    setNextDevelopmentVersion
    ;;
  major)
    releaseMajor
    setNextDevelopmentVersion
    ;;
  -h|--help)
    usage
    ;;
  *)
    echo "Invalid command"
    exit 1
    ;;
esac
