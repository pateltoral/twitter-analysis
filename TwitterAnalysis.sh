#!/bin/sh

#set -e
success=$?

hadoop fs -rm -r -f /twitter/* && 

hive -e "drop table tweets;create table tweets(tweet varchar(100)) row format delimited fields terminated by '|' stored as TEXTFILE location '/twitter/tweets/';drop table positive;create table positive(word varchar(25)) row format delimited fields terminated by '\n' stored as TEXTFILE location '/twitter/words/';"  && 

hadoop fs -put /var/lib/tomcat9/webapps/twitterData.txt /twitter/tweets && 

hadoop fs -put /var/lib/tomcat9/webapps/PositiveWords.txt /twitter/words && 

output=$(hive -e "select count(tweet) from tweets t,positive p where  t.tweet rlike p.word;") && 

echo $output "people feels postive about your place" > /var/lib/tomcat9/webapps/TwitterResult.txt;

if [ "$?" = "0" ] ; then
	echo "Process success"
else 
	echo "------------!!!!!Something wrong !!Process failed!!!!------------";
fi

echo " The hadoop execution for file processing ended at " $(date) ;
