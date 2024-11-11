package io.github.dueris.originspaper.condition.type.meta;

import io.github.dueris.originspaper.OriginsPaper;
import io.github.dueris.originspaper.condition.AbstractCondition;
import io.github.dueris.originspaper.condition.ConditionConfiguration;
import io.github.dueris.originspaper.condition.type.AbstractConditionType;
import io.github.dueris.originspaper.util.context.TypeConditionContext;
import io.github.dueris.calio.data.SerializableData;
import io.github.dueris.calio.data.SerializableDataType;

import java.util.List;
import java.util.function.Function;

public interface AllOfMetaConditionType<T extends TypeConditionContext, C extends AbstractCondition<T, ? extends AbstractConditionType<T, C>>> {

    List<C> conditions();

    default boolean testConditions(T context) {
        return conditions()
            .stream()
            .allMatch(condition -> condition.test(context));
    }

    static <T extends TypeConditionContext, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>, M extends AbstractConditionType<T, C> & AllOfMetaConditionType<T, C>> ConditionConfiguration<M> createConfiguration(SerializableDataType<C> conditionDataType, Function<List<C>, M> constructor) {
        return ConditionConfiguration.of(
            OriginsPaper.apoliIdentifier("all_of"),
            new SerializableData()
                .add("conditions", conditionDataType.list()),
            data -> constructor.apply(
                data.get("conditions")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("conditions", m.conditions())
        );
    }

}
