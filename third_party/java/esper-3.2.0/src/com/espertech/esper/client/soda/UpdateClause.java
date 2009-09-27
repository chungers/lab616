package com.espertech.esper.client.soda;

import com.espertech.esper.collection.Pair;
import com.espertech.esper.util.MetaDefItem;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification for the update clause.
 */
public class UpdateClause implements MetaDefItem, Serializable
{
    private static final long serialVersionUID = 0L;

    private String eventType;
    private String optionalAsClauseStreamName;
    private List<Pair<String, Expression>> assignments;
    private Expression optionalWhereClause;

    /**
     * Ctor.
     * @param eventType the name of the type to update
     * @param propertyName a property to write
     * @param expression expression returning a value to write
     * @return update clause
     */
    public static UpdateClause create(String eventType, String propertyName, Expression expression)
    {
        UpdateClause clause = new UpdateClause(eventType, null);
        clause.addAssignment(propertyName, expression);
        return clause;
    }

    /**
     * Ctor.
     * @param eventType the name of the type to update
     * @param optionalAsClauseStreamName as-clause for update, if any
     */
    public UpdateClause(String eventType, String optionalAsClauseStreamName)
    {
        this.eventType = eventType;
        this.optionalAsClauseStreamName = optionalAsClauseStreamName;
        assignments = new ArrayList<Pair<String, Expression>>();
    }

    /**
     * Adds a property to set to the clause.
     * @param property to set
     * @param expression expression providing the new property value
     * @return clause
     */
    public UpdateClause addAssignment(String property, Expression expression)
    {
        assignments.add(new Pair<String, Expression>(property, expression));
        return this;
    }

    /**
     * Returns the list of property assignments.
     * @return pair of property name and expression
     */
    public List<Pair<String, Expression>> getAssignments()
    {
        return assignments;
    }

    /**
     * Sets a list of property assignments.
     * @param assignments list of pairs of property name and expression
     */
    public void setAssignments(List<Pair<String, Expression>> assignments)
    {
        this.assignments = assignments;
    }

    /**
     * Returns the name of the event type to update.
     * @return name of type
     */
    public String getEventType()
    {
        return eventType;
    }

    /**
     * Returns the name of the event type to update.
     * @param eventType name of type
     */
    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    /**
     * Returns the where-clause if any.
     * @return where clause
     */
    public Expression getOptionalWhereClause()
    {
        return optionalWhereClause;
    }

    /**
     * Sets the where-clause if any.
     * @param optionalWhereClause where clause
     */
    public void setOptionalWhereClause(Expression optionalWhereClause)
    {
        this.optionalWhereClause = optionalWhereClause;
    }

    /**
     * Returns the stream name.
     * @return stream name
     */
    public String getOptionalAsClauseStreamName() {
        return optionalAsClauseStreamName;
    }

    /**
     * Returns the stream name.
     * @param optionalAsClauseStreamName stream name
     */
    public void setOptionalAsClauseStreamName(String optionalAsClauseStreamName) {
        this.optionalAsClauseStreamName = optionalAsClauseStreamName;
    }

    /**
     * Renders the clause in EPL.
     * @param writer to output to
     */
    public void toEPL(StringWriter writer)
    {
        writer.write("update istream ");
        writer.write(eventType);
        if (this.optionalAsClauseStreamName != null) {
            writer.write(" as ");
            writer.write(optionalAsClauseStreamName);
        }
        writer.write(" set ");
        String delimiter = "";
        for (Pair<String, Expression> pair : assignments)
        {
            writer.write(delimiter);
            writer.write(pair.getFirst());
            writer.write(" = ");
            pair.getSecond().toEPL(writer);
            delimiter = ", ";
        }

        if (optionalWhereClause != null)
        {
            writer.write(" where ");
            optionalWhereClause.toEPL(writer);
        }
    }
}