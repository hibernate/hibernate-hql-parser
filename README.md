# Hibernate Query Parser

Experimental new parser for HQL and JPA-QL queries, to convert these into SQL and other different targets such as Lucene queries, Map/Reduce queries for NoSQL stores, make it possible to perform more sophisticated SQL transformations.

### Help

There is much to do and many tests to be ported from the existing parser or added from scratch; anyone is welcome to help by sending pull requests, commenting the code on github directly or by by using the mailing list or IRC chat channels:

* [Mailing list](http://hibernate.org/community/mailinglists)
* [IRC](http://hibernate.org/community/irc)

### Bug Reports:

* [Hibernate JIRA](https://hibernate.onjira.com) (preferred)
* [Mailing list](http://hibernate.org/community/mailinglists)

### Source and credits

The grammar and parser code is based on the _antlr3_ branch previously developed for Hibernate 3; the original code by Steve Ebersole and Alexandre Porcelli can be found on the [archived Hibernate subversion repository](http://anonsvn.jboss.org/repos/hibernate/core/branches/antlr3).

## License

This software and its documentation are distributed under the terms of the FSF Lesser GNU Public License (see license.txt).
