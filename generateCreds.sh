#!/bin/bash

i="0"

while [ "$i" -lt "$2" ]
do
  echo "user$i:$1" >> /credentials
  ((i+=1))
done

exit 0
