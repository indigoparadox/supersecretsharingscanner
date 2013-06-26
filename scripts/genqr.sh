#!/bin/sh

SPLIT_OUTPUT=`java -jar ../libs/secretshare-v1.1.jar split $@`

OLDIFS=$IFS
IFS=$'\n'
for SPLIT_LINE in $SPLIT_OUTPUT; do
   # Only read the bigintcs share lines.
   if ! grep -q "^Share" <<< $SPLIT_LINE || \
      grep -vq bigintcs: <<< $SPLIT_LINE
   then
      continue
   fi

   SHARE_PATTERN="Share \(x:([0-9])+\) = (bigintcs:[A-Za-z0-9\-]+)+"
   SHARE_NUM=`pcregrep -o1 "$SHARE_PATTERN" <<< $SPLIT_LINE`
   SHARE_ITER=`pcregrep -o2 "$SHARE_PATTERN" <<< $SPLIT_LINE`

   qrencode -o share${SHARE_NUM}.png "$SHARE_NUM $SHARE_ITER"
done

