// Copyright (c) 2015 K Team. All Rights Reserved.
package org.kframework;

import org.kframework.attributes.Source;
import org.kframework.definition.Module;
import org.kframework.kore.K;
import org.kframework.kore.Sort;
import org.kframework.parser.concrete2kore.ParseInModule;
import org.kframework.utils.errorsystem.ParseFailedException;
import scala.Option;
import scala.Tuple2;
import scala.util.Either;
import scala.util.control.Exception;

import java.util.HashSet;
import java.util.Set;

/**
 * Parses a string with a particular start symbol/sort.
 * Constructed via the static {@link #from methods}
 */
@API
public class Parser {
    private final ParseInModule parseInModule;
    private Module module;

    private Parser(Module module) {
        this.module = module;
        this.parseInModule = new ParseInModule(module);
    }

    /**
     * Parses a string with a particular start symbol/sort.
     *
     * @param withSort the start symbol/sort
     * @param toParse  the String to parse
     * @param fromSource  the Source of the String toParse
     * @return a pair: the left projection is a parsed string as a K, if successful;
     * the right projection is the set of issues encountered while parsing
     */
    @SuppressWarnings("unchecked")
    public Tuple2<Option<K>, Set<Warning>> apply(Sort withSort, String toParse, Source fromSource) {
        Tuple2<Either<Set<ParseFailedException>, K>, Set<ParseFailedException>> res = parseInModule.parseString(toParse, withSort, fromSource);

        Set<Warning> problemSet = new HashSet<>();
        problemSet.addAll((Set<Warning>) (Object) res._2());
        if (res._1().isLeft())
            problemSet.addAll((Set<Warning>) (Object) res._1().left().get());
        return Tuple2.apply(res._1().right().toOption(), problemSet);
    }

    /**
     * Parses a string with a particular start symbol/sort.
     *
     * @param withSort the start symbol/sort
     * @param toParse  the String to parse
     * @return a pair: the left projection is a parsed string as a K, if successful;
     * the right projection is the set of issues encountered while parsing
     */
    public Tuple2<Option<K>, Set<Warning>> apply(Sort withSort, String toParse) {
        return apply(withSort, toParse, Source.apply("generated"));
    }

    public static Parser from(Module module) {
        return new Parser(module);
    }
}
