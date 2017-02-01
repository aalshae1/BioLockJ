#!/bin/bash
# remove old build & then build/test
rm -rf ./BioLockJ
git clone https://github.com/mikesioda/BioLockJ.git
sleep 4s
cd ./BioLockJ
ant
java -cp ./lib/*:./bin bioLockJ.BioLockJ ./resources/allMiniKraken/krakenAdenonas2015
#java -cp ./lib/*:./bin bioLockJ.BioLockJ ./resources/findResistance/FindCRE_Resistance.BioLockJProperties
