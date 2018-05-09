package xyz.hyperreal.indentation_lexical

import util.parsing.input.{PagedSeq, PagedSeqReader}


object Main extends App {
	val p = new ToyParser

	p.parse( new PagedSeqReader(PagedSeq.fromFile("test")) ) match {
		case p.Success( tree, _ ) => println( ToyInterpreter(tree) )
		case p.NoSuccess( error, rest ) => println( error + "\n" + rest.pos.longString )
	}
}