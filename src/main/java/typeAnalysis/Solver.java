package typeAnalysis;

import baseVisitors.MessageSendCollector;
import math.Literal;
import nullPointerAnalysis.*;
import syntaxtree.Goal;
import syntaxtree.MessageSend;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Solver
{
	/**
	 * if not found, return null.
	 *
	 * @param v
	 * @param constraints
	 * @return
	 */
	public static Literal<AnalysisResult> findLiteral(AnalysisResult v, Collection<EqualityRelationship> constraints)
	{
		Literal<AnalysisResult> literal = null;
		for (EqualityRelationship r : constraints)
		{
			if (r.left.equals(v) && r.right instanceof Literal<?>)
			{
				literal = UnionFunction.union(literal, (Literal<AnalysisResult>) r.right);
			}
		}
		return literal;
	}


	public static boolean checkNullPointer(Goal goal, PrintStream debugOut)
	{
		ConstraintCollector constraintCollector = new ConstraintCollector();
		goal.accept(constraintCollector, null);
		debugOut.println("\nConstraints:");
		for (EqualityRelationship r : constraintCollector.constraints)
		{
			debugOut.println(r);
		}

		//Clear up single union
		Solver.clearUpSingleUnion(constraintCollector.constraints);

		ArrayList<EqualityRelationship> solutions = solve(constraintCollector.constraints);
		debugOut.println("\nSolutions:");
		for (EqualityRelationship r : solutions)
		{
			debugOut.println(r);
		}

		MessageSendCollector messageSendCollector = new MessageSendCollector();
		goal.accept(messageSendCollector);
		for (MessageSend ms : messageSendCollector.messageSends)
		{
			if (solutions.stream().anyMatch(r -> ((VariableRes) r.left).getExpression() == ms.f0 && r.right == PossibleNullLiteral.instance))
			{
				return true;
			}
		}
		return false;
	}

	public static void clearUpSingleUnion(Collection<EqualityRelationship> constraints)
	{
		for (EqualityRelationship r : constraints)
		{
			if (r.right instanceof UnionFunction && ((UnionFunction) r.right).getInput().size() < 2)
			{
				List<AnalysisResult> input = ((UnionFunction) r.right).getInput();
				assert input.size() != 0;
				r.right = input.get(0);
			}
		}
	}

		public static ArrayList<EqualityRelationship> solve(List<EqualityRelationship> constraints)
	{
		HashSet<EqualityRelationship> workingset = new HashSet<>(constraints);

		boolean hasChange;
		do
		{
			hasChange = false;

			constraints = new ArrayList<>(workingset);
			for (int i = 0; i < constraints.size(); i++)
			{
				EqualityRelationship r = constraints.get(i);
				if (r.right instanceof Literal<?> == false)
				{
					if (r.right instanceof FlowSensitiveNullPointerAnalysisVariable)
					{
						Literal<AnalysisResult> result = ((FlowSensitiveNullPointerAnalysisVariable) r.right).getReturnValue(constraints);
						if (result != null)
						{
							EqualityRelationship newEquality = new EqualityRelationship();
							newEquality.left = r.left;
							newEquality.right = (AnalysisResult) result;
							hasChange = workingset.add(newEquality) || hasChange;
						}
					}

					else if (r.right instanceof UnionFunction)
					{
						Literal<AnalysisResult> result = ((UnionFunction) r.right).getReturnValue(constraints);
						if (result != null)
						{
							EqualityRelationship newEquality = new EqualityRelationship();
							newEquality.left = r.left;
							newEquality.right = (AnalysisResult) result;
							hasChange = workingset.add(newEquality) || hasChange;
						}
					}
				}
			}
		}
		while (hasChange);


		ArrayList<EqualityRelationship> solutions = new ArrayList<>();
		for (EqualityRelationship r : workingset)
		{
			if (r.left instanceof VariableRes && r.right instanceof Literal)
				solutions.add(r);
//			if(r.left instanceof VariableOut && r.right instanceof Literal)
//				solutions.add(r);
		}

		return solutions;

	}
}
