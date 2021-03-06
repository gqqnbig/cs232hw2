package numericalAnalysis;

import math.EqualityRelationship;
import math.EquationSolver;
import math.Literal;
import math.Variable;
import utils.Tuple;

import java.util.Collection;

public class PlusInterval implements Variable<Tuple<Interval, Interval>, Interval>, Interval
{
	private final Tuple<Interval, Interval> input;
	public Interval x;
	public Interval y;

	public PlusInterval(Interval x, Interval y)
	{
		input = new Tuple<>(x, y);
		this.x = x;
		this.y = y;
	}

	@Override
	public String getFunctionName()
	{
		return "PlusInterval";
	}

	@Override
	public Tuple<Interval, Interval> getInput()
	{
		return input;
	}

	@Override
	public <ER extends EqualityRelationship<Interval>> Literal<Interval> reduce(Collection<ER> constraints, EquationSolver<Interval> solver)
	{
		LiteralInterval lx = (LiteralInterval) solver.findLiteral(x, constraints);
		if (lx == null)
			return null;

		LiteralInterval ly = (LiteralInterval) solver.findLiteral(y, constraints);
		if (ly == null)
			return null;

		return new LiteralInterval((int)(lx.lowerBound + ly.lowerBound), (int)(lx.upperBound + ly.upperBound));
	}

	@Override
	public String toString()
	{
		return x.toString() + " (+) " + y.toString();
	}
}
