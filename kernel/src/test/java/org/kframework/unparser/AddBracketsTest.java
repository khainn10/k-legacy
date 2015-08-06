// Copyright (c) 2015 K Team. All Rights Reserved.
package org.kframework.unparser;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.kframework.attributes.Source;
import org.kframework.builtin.Sorts;
import org.kframework.definition.Definition;
import org.kframework.definition.Module;
import org.kframework.kompile.Kompile;
import org.kframework.kore.K;
import org.kframework.main.GlobalOptions;
import org.kframework.parser.ProductionReference;
import org.kframework.parser.Term;
import org.kframework.parser.TreeNodesToKORE;
import org.kframework.parser.concrete2kore.ParseInModule;
import org.kframework.parser.concrete2kore.ParserUtils;
import org.kframework.parser.concrete2kore.generator.RuleGrammarGenerator;
import org.kframework.utils.errorsystem.KExceptionManager;
import org.kframework.utils.errorsystem.ParseFailedException;
import org.kframework.utils.file.FileUtil;
import scala.Tuple2;
import scala.util.Either;

import java.io.File;
import java.util.Set;

import static org.junit.Assert.*;

public class AddBracketsTest {

    private RuleGrammarGenerator gen;

    @Before
    public void setUp() throws  Exception{
        gen = makeRuleGrammarGenerator();
    }

    public RuleGrammarGenerator makeRuleGrammarGenerator() {
        String definitionText;
        FileUtil files = FileUtil.testFileUtil();
        ParserUtils parser = new ParserUtils(files, new KExceptionManager(new GlobalOptions()));
        File definitionFile = new File(Kompile.BUILTIN_DIRECTORY.toString() + "/kast.k");
        definitionText = files.loadFromWorkingDirectory(definitionFile.getPath());

        Definition baseK =
                parser.loadDefinition("K", "K", definitionText,
                        Source.apply(definitionFile.getAbsolutePath()),
                        definitionFile.getParentFile(),
                        Lists.newArrayList(Kompile.BUILTIN_DIRECTORY),
                        true);

        return new RuleGrammarGenerator(baseK, true);
    }

    private Module parseModule(String def) {
        return ParserUtils.parseMainModuleOuterSyntax(def, Source.apply("generated by AddBracketsTest"), "TEST");
    }


    private String unparseTerm(K input, Module test) {
        return KOREToTreeNodes.toString(new AddBrackets(test).addBrackets((ProductionReference) KOREToTreeNodes.apply(KOREToTreeNodes.up(input), test)));
    }


    @Test
    public void testLambda() {
        String def = "module TEST\n" +
                "  syntax Val ::= Id\n" +
                "               | \"lambda\" Id \".\" Exp\n" +
                "  syntax Exp ::= Val\n" +
                "               | Exp Exp      [left]\n" +
                "               | \"(\" Exp \")\"  [bracket]\n" +
                "  syntax Id ::= r\"(?<![A-Za-z0-9\\\\_])[A-Za-z\\\\_][A-Za-z0-9\\\\_]*\"     [notInRules, token, autoreject]\n" +
                "endmodule\n";
        unparserTest(def, "( lambda z . ( z z ) ) lambda x . lambda y . ( x y )");
        unparserTest(def, "a ( ( lambda x . lambda y . x ) y z )");
    }

    @Test
    public void testPriorityAndAssoc() {
        String def = "module TEST\n" +
                "  syntax Exp ::= Exp \"+\" Exp [left]\n" +
                "  syntax Exp ::= Exp \"*\" Exp [left]\n" +
                "  syntax Exp ::= \"1\"\n" +
                "  syntax Exp ::= \"(\" Exp \")\" [bracket]\n" +
                "  syntax priority '_*_ > '_+_\n" +
                "endmodule\n";
        unparserTest(def, "1 + 1 + 1 + 1");
        unparserTest(def, "1 + ( 1 + 1 ) + 1");
        unparserTest(def, "1 + ( 1 + ( 1 + 1 ) )");
        unparserTest(def, "1 + 1 * 1");
        unparserTest(def, "( 1 + 1 ) * 1");
    }

    private void unparserTest(String def, String pgm) {
        Module test = parseModule(def);
        ParseInModule parser = gen.getCombinedGrammar(gen.getProgramsGrammar(test));
        K parsed = parseTerm(pgm, parser);
        String unparsed = unparseTerm(parsed, test);
        assertEquals(pgm, unparsed);
    }

    private K parseTerm(String pgm, ParseInModule parser) {
        Tuple2<Either<Set<ParseFailedException>, Term>, Set<ParseFailedException>> result = parser.parseString(pgm, Sorts.K(), Source.apply("generated by AddBracketsTest"));
        assertEquals(0, result._2().size());
        return TreeNodesToKORE.down(TreeNodesToKORE.apply(result._1().right().get()));
    }

}