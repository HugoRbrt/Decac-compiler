#! /bin/sh

cd "$(dirname "$0")"/../../.. || exit 1
./Launch-test.sh synt invalid HelloWorld

exit $?