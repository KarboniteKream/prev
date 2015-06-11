#!/bin/bash

find src/ -name "*.class" -type f -delete
find tests/ \( -name "*.log" -o -name "*.mms" -o -name "*.mmo" \) -type f -delete
