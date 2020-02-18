package nullPointerAnalysis;

import baseVisitors.ParameterCollector;
import syntaxtree.*;
import utils.Location;
import utils.Scope;

import java.util.*;

public class ProgramStructureCollector extends typeAnalysis.ProgramStructureCollector
{
	static ArrayList<ObjectIdentifierDefinition> nullables;
	static HashMap<Tuple, Location> lastStatementData;
//	static HashMap<Tuple, ObjectIdentifierDefinition> methodParameterInfos;

//region static methods

	public static boolean isNullable(Type type)
	{
		if (type.f0.choice instanceof BooleanType)
			return false;
		if (type.f0.choice instanceof IntegerType)
			return false;

		assert type.f0.choice instanceof Identifier || type.f0.choice instanceof ArrayType;
		return true;
	}

	public static void init(Goal goal)
	{
		goal.accept(new ProgramStructureCollector());
	}


	private static List<ObjectIdentifierDefinition> getNullableFieldsDefinedInClass(String className)
	{
		List<ObjectIdentifierDefinition> scopeNullables = new ArrayList<>();
		for (ObjectIdentifierDefinition entry : nullables)
		{
			if (entry.Class.equals(className) && entry.Method == null)
				scopeNullables.add(entry);
		}
		return scopeNullables;
	}

	public static List<ObjectIdentifierDefinition> getNullableIdentifiersInScope(Scope scope)
	{
		List<ObjectIdentifierDefinition> scopeNullables = new ArrayList<>();
		for (ObjectIdentifierDefinition entry : nullables)
		{
			if (entry.Class.equals(scope.Class) &&
					(entry.Method == null || entry.Method.equals(scope.Method))) //fields are available in a method.
				scopeNullables.add(entry);
		}

		String className = typeAnalysis.ClassHierarchyAnalysis.superClassHierarchy.get(scope.Class);
		while (className != null)
		{
			scopeNullables.addAll(getNullableFieldsDefinedInClass(className));
			className = superClassHierarchy.get(className);
		}

		return scopeNullables;
	}

	/**
	 * Get definition of a nullable identifier from the identifier usage.
	 * <p>
	 * If the identifier is not nullable, eg. int, return null.
	 *
	 * @param identifier
	 * @return
	 */
	public static ObjectIdentifierDefinition getDefinition(Identifier identifier, Scope scope)
	{
		ObjectIdentifierDefinition fieldDefinition = null;
		for (var d : getNullableIdentifiersInScope(scope))
		{
			if (d.getIdentifier().equals(identifier.f0.toString()))
			{
				if (d.Method == null)
				{
					assert fieldDefinition == null;
					fieldDefinition = d;
				}
				else
					return d;
			}
		}
		return fieldDefinition;
	}


	public static Location getFirstStatement(String className, String methodName)
	{
		return null;
	}

	public static Location getLastStatement(String className, String methodName)
	{
		Tuple key = new Tuple();
		key.item1 = className;
		key.item2 = methodName;

		return lastStatementData.get(key);
	}

	/**
	 *
	 * @param className
	 * @param methodName
	 * @param parameterIndex
	 * @return null if the parameter is not of type object.
	 */
	public static ObjectIdentifierDefinition getParameter(String className, String methodName, int parameterIndex)
	{
		var parameter = nullables.stream().filter(o -> o.parameterIndex == parameterIndex && o.Class.equals(className) && Objects.equals(o.Method, methodName)).findAny();
		// parameter may be null if the parameter is not object, eg. int.
		if (parameter.isEmpty())
			return null;
		else
			return parameter.get();
	}
	//endregion

	Location lastStatement;

	protected ProgramStructureCollector()
	{
		nullables = new ArrayList<>();
		lastStatementData = new HashMap<>();
//		methodParameterInfos = new HashMap<>();
	}


	@Override
	public Object visitScope(MainClass n)
	{
		super.visitScope(n);
		nullables.add(new ObjectIdentifierDefinition(n.f11, getClassName()));

		n.f14.accept(this);
		n.f15.accept(this);
		if (lastStatement != null)
		{
			Tuple key = new Tuple();
			key.item1 = getClassName();
			key.item2 = getMethodName();
			lastStatementData.put(key, lastStatement);
		}
		lastStatement = null;

		return null;
	}

	@Override
	public Object visitScope(MethodDeclaration n)
	{
		super.visitScope(n);

		ParameterCollector parameterCollector = new ParameterCollector();
		n.f4.accept(parameterCollector);
		ArrayList<FormalParameter> parameters = parameterCollector.parameters;
		for (int i = 0; i < parameters.size(); i++)
		{
			FormalParameter p = parameters.get(i);
			if (isNullable(p.f0))
				nullables.add(new ObjectIdentifierDefinition(p.f1, getClassName(), getMethodName(), i));
		}


		n.f7.accept(this);


		lastStatement = new Location(n.f9);
		Tuple key = new Tuple();
		key.item1 = getClassName();
		key.item2 = getMethodName();
		lastStatementData.put(key, lastStatement);

		lastStatement = null;
		return null;
	}

	@Override
	public Object visitScope(ClassDeclaration n)
	{
		assert classMethodMapping.containsKey(n.f1.f0.toString()) == false;
		classMethodMapping.put(n.f1.f0.toString(), new HashSet<>());


		n.f3.accept(this);
		n.f4.accept(this);
		return null;
	}

	@Override
	protected Object visitScope(ClassExtendsDeclaration n)
	{
		superClassHierarchy.put(n.f1.f0.toString(), n.f3.f0.toString());

		assert classMethodMapping.containsKey(n.f1.f0.toString()) == false;
		classMethodMapping.put(n.f1.f0.toString(), new HashSet<>());


		n.f5.accept(this);
		n.f6.accept(this);
		return null;
	}

	@Override
	public Object visit(VarDeclaration n)
	{
		if (isNullable(n.f0))
			nullables.add(new ObjectIdentifierDefinition(n.f1, getClassName(), getMethodName(), -1));
		return null;
	}


	@Override
	public Object visit(Statement n)
	{
		lastStatement = new Location(n);
		return super.visit(n);
	}
}

class Tuple
{
	public String item1;
	public String item2;


	public Tuple()
	{
	}

	public Tuple(String item1, String item2)
	{
		this.item1 = item1;
		this.item2 = item2;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Tuple tuple = (Tuple) o;
		return Objects.equals(item1, tuple.item1) &&
				Objects.equals(item2, tuple.item2);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(item1, item2);
	}
}