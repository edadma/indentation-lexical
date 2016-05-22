package ca.hyperreal.indentation

import collection.mutable.{HashMap, ListBuffer}

import ca.hyperreal.lia._


object ToyInterpreter
{
	var vars: HashMap[String, Any] = null

	def apply( t: List[Any] ) =
	{
		vars = new HashMap
		vars("e") = math.E
		vars("pi") = math.Pi
		vars("i") = Complex( 0, 1 )

		run( t )
	}

	def neval( e: Any ) = eval( e ).asInstanceOf[Number]

	def beval( e: Any ) = eval( e ).asInstanceOf[Boolean]

	def eval( e: Any ): Any =
		e match
		{
			case ("#assign", (v: String, e)) => vars(v) = eval( e )
			case ("#if", c, t, ei: List[(Any, Any)], e: Option[Any]) =>
				((c, t) +: ei) find (i => beval(i._1)) match
				{
					case Some( (_, t) ) => eval( t )
					case None =>
						if (e == None)
							()
						else
							eval( e.get )
				}
			case ("print", args: List[Any]) => println( (args map (eval(_))) mkString ", " )
			case ("double", List(a)) => neval( a ).doubleValue
			case ("decimal", List(a)) => Math.toBigDecimal( neval(a) )
			case ("#var", v: String) => vars(v)
			case (o @ ("+" | "-" | "*" | "/" | "^" | "=="), (l, r)) => Math( Symbol(o.asInstanceOf[String]), eval(l), eval(r) )
			case ("-", e) => Math( '-, eval(e) )
			case ("#list", l: List[Any]) =>
				val buf = new ListBuffer[Any]

				l map (e => buf append eval( e ))

				buf.toList
			case ("#block", s: List[Any]) => run( s )
			case ("or", (l, r)) => beval( l ) || beval( r )
			case ("and", (l, r)) => beval( l ) && beval( r )
			case ("not", e) => !beval( e )
			case _ => e
		}

	def run( t: List[Any] ): Any =
		t match {
			case Nil => null
			case List( s ) => eval( s )
			case s :: tail =>
				eval( s )
				run( tail )
		}
}