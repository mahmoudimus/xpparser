#!/bin/bash
printf "%'.0f" `grep '<schemas>' ../../benchmark/*-axis-step.xml | wc -l`
