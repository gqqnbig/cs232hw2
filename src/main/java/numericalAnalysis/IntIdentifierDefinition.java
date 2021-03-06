package numericalAnalysis;

import syntaxtree.Identifier;
import utils.Options;

import java.util.Objects;

public class IntIdentifierDefinition
{
	private final String identifier;
	public String Class;
	public String Method;
	public int parameterIndex;

	public String getIdentifier()
	{
		return identifier;
	}

	/**
	 * Only NullableCollector should call this.
	 *
	 * @param identifier
	 * @param className
	 * @param methodName
	 * @param parameterIndex -1 means not a parameter.
	 */
	public IntIdentifierDefinition(Identifier identifier, String className, String methodName, int parameterIndex)
	{

		this.identifier = identifier.f0.toString();
		Class = className;
		Method = methodName;
		this.parameterIndex = parameterIndex;
	}

	public IntIdentifierDefinition(Identifier mainMethodArgs, String className)
	{

		this.identifier = mainMethodArgs.f0.toString();
		Class = className;
		Method = "main";
		parameterIndex = 0;
	}

	public boolean getIsParameter()
	{
		return parameterIndex > -1;
	}

	public String toString()
	{
		if (Options.shortform)
		{
			if (Method == null)
				return String.format("%2$s.%1$s", identifier, Class);

			if (parameterIndex >= 0)
				return String.format("%2$s.%3$s#%1$s", identifier, Class, Method);

			return String.format("%2$s.%3$s.%1$s", identifier, Class, Method);
		}
		else
		{
			if (Method == null)
				return String.format("Field %s defined in %s", identifier, Class);

			if (parameterIndex >= 0)
				return String.format("Parameter %s defined in %s.%s", identifier, Class, Method);

			return String.format("Variable %s defined in %s.%s", identifier, Class, Method);
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IntIdentifierDefinition that = (IntIdentifierDefinition) o;
		return parameterIndex == that.parameterIndex &&
				identifier.equals(that.identifier) &&
				Class.equals(that.Class) &&
				Objects.equals(Method, that.Method);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(identifier, Class, Method, parameterIndex);
	}
}

