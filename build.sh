#!/usr/bin/env bash
set -euo pipefail
rm -rf build
mkdir -p build
java -jar patcher/target/patcher.jar baseline/MagicSMP.jar helper/target/magicsmp-sell-helper-1.0.jar build/MagicSMP.jar
jar tf build/MagicSMP.jar | grep -q 'com/bx/magicSmp/menus/SellMenuClickBridge.class'
echo "Built build/MagicSMP.jar"
