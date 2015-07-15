package ca.hyperreal.indentation

import util.parsing.combinator.lexical.StdLexical
import util.parsing.combinator.token.Tokens
import util.parsing.input.{CharSequenceReader, Position, Reader}
import collection.mutable.{ListBuffer, ArrayStack}


class IndentationLexical( newlineBeforeIndent: Boolean, newlineAfterDedent: Boolean,
	startLineJoining: List[String], endLineJoining: List[String], lineComment: String, blockCommentStart: String, blockCommentEnd: String ) extends StdLexical
{
	private val level = new ArrayStack[Int]
	private var state: Int = _
	private var current: Int = _
	private var finalnl = false
	private var dedentnl = false
	private var lineJoining = 0
	private val startLineJoiningTokens = startLineJoining map (Keyword(_))
	private val endLineJoiningTokens = endLineJoining map (Keyword(_))
	
	case object Newline extends Token {val chars = "newline"}
	case object Indent extends Token {val chars = "indent"}
	case object Dedent extends Token {val chars = "dedent"}

	def num( s: String ) = NumericLit( s )
	
	def scan( s: String ): List[Token] =
	{
	val buf = new ListBuffer[Token]
	var t = read( new CharSequenceReader(s) )
	
		while (!t.atEnd)
		{
			buf append t.first
			t = t.rest
		}

		buf.toList
	}

	private def matches( r: Input, s: String ): Boolean = {
		var input = r
		
		for (i <- 0 until s.length)
			if (!input.atEnd && s.charAt( i ) == input.first)
				input = input.rest
			else
				return false
		
		true
	}
	
	private def skip( r: Input, pred: Input => Boolean ): Input =
		if (pred( r ))
			r
		else
			skip( r.rest, pred )
			
	private def skipSpace( r: Input ) = skip( r, a => a.atEnd || a.first != '\t' && a.first != ' ' )

	private def skipToEOL( r: Input ) = skip( r, a => a.atEnd || a.first == '\n' )

	private def skipBlankLines( r: Input ): Input =
		if (r.atEnd)
			r
		else
		{
		val r1 = skipSpace( r )

			if (r1.atEnd)
				r1
			else if (r1.first == '\n')
				skipBlankLines( r1.rest )
			else if (matches( r1, lineComment ))
			{
			val r2 = skipToEOL( r1.rest.rest )

				if (r2.atEnd)
					r2
				else
					skipBlankLines( r2.rest )
			}
			else if (matches( r1, blockCommentStart ))
			{
			val r2 = skip( r1.rest.rest, a => matches( a, blockCommentEnd ))

				if (r2.atEnd) sys.error( "unclosed comment " + r1.pos )

				skipBlankLines( r2.rest.rest )
			}
			else
				r
		}

	def read( in: Input ): Reader[Token] =
	{
		level.clear
		level push 0
		state = BLOCK_STATE
		finalnl = false
		dedentnl = false
		lineJoining = 0
		new IndentationScanner( skipBlankLines(in) )
	}

	override def whitespaceChar = elem( "space char", c => c == ' ' || c == '\t' || c == '\r' )

	private val BLOCK_STATE = 1
	private val INDENT_STATE = 2
	private val DEDENT_STATE = 3
	private val NEWLINE_STATE = 4

	class IndentationScanner( in: Input ) extends Reader[Token]
	{
		private def skipWhiteSpace( r: Input ): ParseResult[Any] =
		{
			whitespace( r ) match
			{
				case res@Success(_, in1) =>
					if (!in1.atEnd && in1.first == '\n')
						skipWhiteSpace( in1.rest )
					else
						res
				case res => res
			}
		}
		
		private val (tok, rest1, rest2) =
		{
			whitespace( in ) match
			{
				case Success(_, in0) => 
					IndentationParser( in0 ) match
					{
						case Success(tok, in2) =>
							(tok, in, in2)
						case NoSuccess(_, _) =>
							skipWhiteSpace( in ) match {
								case Success(_, in1) =>
									token(in1) match {
										case Success(tok, in2) =>
											if (startLineJoiningTokens contains tok)
												lineJoining += 1
											else if (endLineJoiningTokens contains tok)
												lineJoining -= 1

											(tok, in1, in2)
										case ns: NoSuccess => (errorToken(ns.msg), ns.next, skip(ns.next))
									}
								case ns: NoSuccess => (errorToken(ns.msg), ns.next, skip(ns.next))
							}
					}
				case ns: NoSuccess => (errorToken(ns.msg), ns.next, skip(ns.next))
			}
		}
	
		private def skip( in: Reader[Char] ) = if (in.atEnd) in else in.rest

		override def source: java.lang.CharSequence = in.source

		override def offset: Int = in.offset

		private def atend = in.atEnd || (skipWhiteSpace(in) match { case Success(_, in1) => in1.atEnd case _ => false })

		val atEnd = atend && finalnl && !dedentnl && level.size == 1

		lazy val first = gettoken

		private def gettoken =
		{
 		val res =
			if (atend)
				if (!finalnl || dedentnl)
					Newline
				else if (level.size > 1)
					Dedent
				else
					sys.error( "no more tokens" )
			else
				tok

//			println( res )
			res
		}

		lazy val rest =
 			if (atend)
				if (!finalnl)
				{
					finalnl = true
					new IndentationScanner( rest1 )
				}
				else if (dedentnl)
				{
					dedentnl = false
					new IndentationScanner( rest1 )
				}
				else if (level.size > 1)
				{
					if (newlineAfterDedent)
						dedentnl = true
						
					level.pop
					new IndentationScanner( rest1 )
				}
				else
					sys.error( "no more tokens" )
			else
				new IndentationScanner( rest2 )

		lazy val pos = new PositionWrapper( rest1.pos )
	}

	class PositionWrapper( p: Position ) extends Position
	{
		val column = p.column

		val line = p.line

		protected val lineContents = p.longString.split( "\n" )(0)
	}
	
	private object IndentationParser extends Parser[Token]
	{
		def apply( in: Input ): ParseResult[Token] =
		{
			def indents( ch: Char, c: Int, r: Input ): (Int, Input) =
				if (ch != ' ' && ch != '\t' || r.atEnd || r.first != ch)
					(c, r)
				else
					indents( ch, c + 1, r.rest )

			state match
			{
				case BLOCK_STATE =>
					if (in.atEnd || in.first != '\n')
						Failure( null, in )
					else if (lineJoining > 0)
						Failure( null, in )
					else
					{
					val in1 = skipBlankLines( in.rest )

						if (in1.atEnd)
						{
//							finalnl = true
							Failure( null, in1 )
						}
						else
						{
						val (c, r) = indents( in1.first, 0, in1 )

							if (skipSpace( in1 ).pos != r.pos)
								Error( "only tabs or spaces (but not both on a given line) may be used for indentation", in1 )
							else
							{
								if (c > level.top)
								{
									level push c
									
									if (newlineBeforeIndent)
										state = INDENT_STATE
									else
										return Success( Indent, r )
								}
								else if (c < level.top)
								{
									current = c
									state = DEDENT_STATE
									level pop
								}

								Success( Newline, r )
							}
						}
					}
				case INDENT_STATE =>
					state = BLOCK_STATE
					Success( Indent, in )
				case DEDENT_STATE =>
					if (newlineAfterDedent)
						state = NEWLINE_STATE
					else
						if (current < level.top)
							level pop
						else
							state = BLOCK_STATE

					Success( Dedent, in )
				case NEWLINE_STATE =>
					if (current < level.top)
					{
						state = DEDENT_STATE
						level pop
					}
					else
						state = BLOCK_STATE

					Success( Newline, in )
			}
		}
	}
}