#!/bin/bash

# build/test

# git clone https://github.com/mikesioda/BioLockJ.git
# sleep 4s
#cd ./BioLockJ
ant

java -cp ./lib/*:./bin bioLockJ.BioLockJ ./resources/obesityFeb2017.properties
#java -cp ./lib/*:./bin bioLockJ.BioLockJ ./resources/allMiniKraken/krakenAdenonas2015
#java -cp ./lib/*:./bin bioLockJ.BioLockJ ./resources/findResistance/FindCRE_Resistance.BioLockJProperties
