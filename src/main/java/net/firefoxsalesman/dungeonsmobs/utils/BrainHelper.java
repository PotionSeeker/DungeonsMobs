package net.firefoxsalesman.dungeonsmobs.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.firefoxsalesman.dungeonsmobs.mixin.BrainAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.schedule.Activity;

public class BrainHelper {

    /*
    public static <E extends LivingEntity> void addActivityAndRemoveMemoryWhenStopped(Brain<E> brain, Activity activity, int priorityStart, ImmutableList<? extends Task<? super E>> tasks, MemoryModuleType<?> memoryToRemove) {
        Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> memoryToStatusSet = ImmutableSet.of(Pair.of(memoryToRemove, MemoryModuleStatus.VALUE_PRESENT));
        Set<MemoryModuleType<?>> memorySet = ImmutableSet.of(memoryToRemove);
        addActivityAndRemoveMemoriesWhenStopped(brain, activity, createPriorityPairs(priorityStart, tasks), memoryToStatusSet, memorySet);
    }
    */

    public static <E extends LivingEntity> ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> createPriorityPairs(int priorityStart, ImmutableList<? extends BehaviorControl<? super E>> tasks) {
        int priorityIndex = priorityStart;
        ImmutableList.Builder<Pair<Integer, ? extends BehaviorControl<? super E>>> priorityPairs = ImmutableList.builder();

        for (BehaviorControl<? super E> task : tasks) {
            priorityPairs.add(Pair.of(priorityIndex++, task));
        }

        return priorityPairs.build();
    }

    /*
    private static <E extends LivingEntity> void addActivityAndRemoveMemoriesWhenStopped(Brain<E> brain, Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> prioritizedTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> memoryToStatusSet, Set<MemoryModuleType<?>> memorySet){
        BrainAccessor<E> brainAccessor = castToAccessor(brain);
        brainAccessor.getActivityRequirements().put(activity, memoryToStatusSet);
        if (!memorySet.isEmpty()) {
            brainAccessor.getActivityMemoriesToEraseWhenStopped().put(activity, memorySet);
        }

        addPrioritizedBehaviors(activity, prioritizedTasks, brainAccessor);
    }

    public static <E extends LivingEntity> void addPrioritizedBehaviors(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> prioritizedTasks, BrainAccessor<E> brainAccessor) {
        for(Pair<Integer, ? extends Task<? super E>> pair : prioritizedTasks) {
            brainAccessor.getAvailableBehaviorsByPriority()
                    .computeIfAbsent(pair.getFirst(), (p) -> Maps.newHashMap())
                    .computeIfAbsent(activity, (a) -> Sets.newLinkedHashSet())
                    .add(pair.getSecond());
        }
    }
     */

    public static <E extends LivingEntity> void addPrioritizedBehaviors(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> prioritizedTasks, Brain<E> brain) {
        BrainAccessor<E> brainAccessor = castToAccessor(brain);

        for (Pair<Integer, ? extends BehaviorControl<? super E>> pair : prioritizedTasks) {
            brainAccessor.getAvailableBehaviorsByPriority()
                    .computeIfAbsent(pair.getFirst(), (p) -> Maps.newHashMap())
                    .computeIfAbsent(activity, (a) -> Sets.newLinkedHashSet())
                    .add(pair.getSecond());
        }
    }

    public static <E extends LivingEntity> BrainAccessor<E> castToAccessor(Brain<E> brain) {
        //noinspection unchecked
        return (BrainAccessor<E>) brain;
    }
}
