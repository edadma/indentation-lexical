package funl.indentation

import util.parsing.input.CharSequenceReader

import funl.lia._


object Main extends App
{
 	val code =
"""
a =
 if true then
  123
print( a )
"""
// """
// 1
// 	2
// 		3
// 4
// """

	var r = ToyParser.lexical.read( new CharSequenceReader(code) )
	
// 	while (!r.atEnd)
// 	{
// 		println( r.first )
// 		r = r.rest
// 	}
	
	ToyParser.parse( new CharSequenceReader(code) ) match
	{
		case ToyParser.Success( tree, _ ) =>
//			println( tree )
			ToyInterpreter( tree )
		case ToyParser.NoSuccess( error, rest ) => println( error + "\n" + rest.pos.longString )
	}
}