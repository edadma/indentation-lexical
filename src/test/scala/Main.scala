package ca.hyperreal.indentation

import collection.immutable.PagedSeq
import util.parsing.input.PagedSeqReader


object Main extends App
{
	val p = new ToyParser

	p.parse( new PagedSeqReader(PagedSeq.fromFile("test")) ) match
	{
		case p.Success( tree, _ ) => println( ToyInterpreter(tree) )
		case p.NoSuccess( error, rest ) => println( error + "\n" + rest.pos.longString )
	}
}