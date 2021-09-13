//package com.af.camerap;
//
//import android.app.Activity;
//import android.app.Application;
//import android.os.Build;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
//import androidx.lifecycle.CompositeGeneratedAdaptersObserver;
//import androidx.lifecycle.FullLifecycleObserver;
//import androidx.lifecycle.FullLifecycleObserverAdapter;
//import androidx.lifecycle.GeneratedAdapter;
//import androidx.lifecycle.Lifecycle;
//import androidx.lifecycle.LifecycleEventObserver;
//import androidx.lifecycle.LifecycleObserver;
//import androidx.lifecycle.LifecycleOwner;
//import androidx.lifecycle.LifecycleRegistry;
//import androidx.lifecycle.Lifecycling;
//import androidx.lifecycle.ReflectiveGenericLifecycleObserver;
//import androidx.lifecycle.ReportFragment;
//import androidx.lifecycle.SingleGeneratedAdapterObserver;
//
//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.EventBusException;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.SubscriberMethod;
//import org.greenrobot.eventbus.SubscriberMethodFinder;
//import org.greenrobot.eventbus.ThreadMode;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.logging.Level;
//
//import static androidx.lifecycle.Lifecycle.State.DESTROYED;
//import static androidx.lifecycle.Lifecycle.State.INITIALIZED;
//
///**
// * Project    CameraP
// * Path       com.af.camerap
// * Date       2021/09/01 - 10:11
// * Author     Payne.
// * About      类描述：
// */
//public class Test {
//
//    public static void injectIfNeededIn(Activity activity) {
//        //API29及其以上，监听activity什么周期的逻辑
//        if (Build.VERSION.SDK_INT >= 29) {
//            // On API 29+, we can register for the correct Lifecycle callbacks directly
//            ReportFragment.LifecycleCallbacks.registerIn(activity);
//        }
//
//        //API29以下，通过在activity添加一个空UI的Fragment，然后监听Fragment的生命周期就是Activity的生命周期
//        // Prior to API 29 and to maintain compatibility with older versions of
//        // ProcessLifecycleOwner (which may not be updated when lifecycle-runtime is updated and
//        // need to support activities that don't extend from FragmentActivity from support lib),
//        // use a framework fragment to get the correct timing of Lifecycle events
//        android.app.FragmentManager manager = activity.getFragmentManager();
//        if (manager.findFragmentByTag(REPORT_FRAGMENT_TAG) == null) {
//            manager.beginTransaction().add(new ReportFragment(), REPORT_FRAGMENT_TAG).commit();
//            // Hopefully, we are the first to make a transaction.
//            manager.executePendingTransactions();
//        }
//    }
//
//    @RequiresApi(29)
//    static class LifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
//
//        static void registerIn(Activity activity) {
//            //通过activity提供的接口，监听生命周期，是在API29才加入的功能
//            activity.registerActivityLifecycleCallbacks(new ReportFragment.LifecycleCallbacks());
//        }
//
//        @Override
//        public void onActivityPostCreated(@NonNull Activity activity,
//                                          @Nullable Bundle savedInstanceState) {
//            //监听到相应的生命周期，分发事件
//            dispatch(activity, Lifecycle.Event.ON_CREATE);
//        }
//
//        //......其他事件
//
//        @Override
//        public void onActivityPrePaused(@NonNull Activity activity) {
//            dispatch(activity, Lifecycle.Event.ON_PAUSE);
//        }
//    }
//
//    public void addObserver(@NonNull LifecycleObserver observer) {
//        //确保在主线程运行
//        //当mEnforceMainThread为true时，必须是在主线程调用，否则抛出异常
//        enforceMainThreadIfNeeded("addObserver");
//
//        //初始化生命周期状态，如果当前状态不是DESTROYED，从INITIALIZED状态开始
//        Lifecycle.State initialState = mState == DESTROYED ? DESTROYED : INITIALIZED;
//
//        //根据观察者和初始化状态，生成ObserverWithState实例
//        //先把所有类型的观察者都转成LifecycleEventObserver类型，具体流程后面再说
//        ObserverWithState statefulObserver = new ObserverWithState(observer, initialState);
//
//        //判断观察者是否已经添加到map，如果已经添加，则直接返回
//        ObserverWithState previous = mObserverMap.putIfAbsent(observer, statefulObserver);
//        if (previous != null) {
//            return;
//        }
//
//        //获取LifecycleOwner对象实例，一般来说是Activity或者Fragment实例
//        //为了避免内存泄露，使用弱引用，如果对象为空，说明已经被回收了，直接返回
//        LifecycleOwner lifecycleOwner = mLifecycleOwner.get();
//        if (lifecycleOwner == null) {
//            // it is null we should be destroyed. Fallback quickly
//            return;
//        }
//
//        boolean isReentrance = mAddingObserverCounter != 0 || mHandlingEvent;
//
//        //计算目标状态，目的是从初始化状态遍历到当前实际状态，然后把每一个状态的事件分发
//        Lifecycle.State targetState = calculateTargetState(observer);
//        mAddingObserverCounter++;
//        while ((statefulObserver.mState.compareTo(targetState) < 0
//                && mObserverMap.contains(observer))) {
//            pushParentState(statefulObserver.mState);
//
//            //获取目标状态的后一个事件，比如当前目标状态是CREATED状态，则返回ON_START事件
//            final Lifecycle.Event event = Event.upFrom(statefulObserver.mState);
//            if (event == null) {
//                throw new IllegalStateException("no event up from " + statefulObserver.mState);
//            }
//
//            //分发事件
//            statefulObserver.dispatchEvent(lifecycleOwner, event);
//
//            popParentState();
//            // mState / subling may have been changed recalculate
//            targetState = calculateTargetState(observer);
//        }
//
//        if (!isReentrance) {
//            // we do sync only on the top level.
//            sync();
//        }
//        mAddingObserverCounter--;
//    }
//
//
//    //Lifecycle 类中对 addObserver 方法添加了 @MainThread 注解，意思是该方法只能用于主线程调用
//    //所以此处不考虑多线程的情况
//    @Override
//    public void addObserver(@NonNull LifecycleObserver observer) {
//        State initialState = mState == DESTROYED ? DESTROYED : INITIALIZED;
//        ObserverWithState statefulObserver = new ObserverWithState(observer, initialState);
//        ObserverWithState previous = mObserverMap.putIfAbsent(observer, statefulObserver);
//
//        if (previous != null) {
//            //如果 observer 之前已经传进来过了，则不重复添加，直接返回
//            return;
//        }
//        LifecycleOwner lifecycleOwner = mLifecycleOwner.get();
//        if (lifecycleOwner == null) {
//            //如果 LifecycleOwner 对象已经被回收了，则直接返回
//            return;
//        }
//
//        //如果 isReentrance 为 true，意味着当前存在重入的情况：
//        //1. mAddingObserverCounter != 0。会出现这种情况，
//        // 是由于开发者先添加了一个 LifecycleObserver ，当还在向其回调事件的过程中，
//        // 在回调方法里又再次调用了 addObserver 方法添加了一个新的 LifecycleObserver
//
//        //2.mHandlingEvent 为 true。即此时正处于向外回调 Lifecycle.Event 的状态
//        boolean isReentrance = mAddingObserverCounter != 0 || mHandlingEvent;
//
//        State targetState = calculateTargetState(observer);
//
//        //递增加一，标记当前正处于向新添加的 LifecycleObserver 回调 Event 值的过程
//        mAddingObserverCounter++;
//
//        //statefulObserver.mState.compareTo(targetState) < 0 成立的话说明 State 值还没遍历到目标状态
//        //mObserverMap.contains(observer) 成立的话说明 observer 还没有并移除
//        //因为有可能在遍历过程中开发者主动在回调方法里将 observer 给移除掉了，所以这里每次循环都检查下
//        while ((statefulObserver.mState.compareTo(targetState) < 0
//                && mObserverMap.contains(observer))) {
//            //将 observer 已经遍历到的当前的状态值 mState 保存下来
//            pushParentState(statefulObserver.mState);
//            //向 observer 回调进入“statefulObserver.mState”前需要收到的 Event 值
//            statefulObserver.dispatchEvent(lifecycleOwner, upEvent(statefulObserver.mState));
//            //移除 mState
//            popParentState();
//            // mState / subling may have been changed recalculate
//            targetState = calculateTargetState(observer);
//        }
//
//        if (!isReentrance) {
//            // we do sync only on the top level.
//            sync();
//        }
//        mAddingObserverCounter--;
//    }
//
//    private void forwardPass(LifecycleOwner lifecycleOwner) {
//        //遍历所有的观察者，分发事件
//        Iterator<Map.Entry<LifecycleObserver, LifecycleRegistry.ObserverWithState>> ascendingIterator =
//                mObserverMap.iteratorWithAdditions();
//        while (ascendingIterator.hasNext() && !mNewEventOccurred) {
//            Map.Entry<LifecycleObserver, LifecycleRegistry.ObserverWithState> entry = ascendingIterator.next();
//            LifecycleRegistry.ObserverWithState observer = entry.getValue();
//            while ((observer.mState.compareTo(mState) < 0 && !mNewEventOccurred
//                    && mObserverMap.contains(entry.getKey()))) {
//                pushParentState(observer.mState);
//                final Lifecycle.Event event = Event.upFrom(observer.mState);
//                if (event == null) {
//                    throw new IllegalStateException("no event up from " + observer.mState);
//                }
//                //具体的事件分发逻辑
//                observer.dispatchEvent(lifecycleOwner, event);
//                popParentState();
//            }
//        }
//    }
//
//    static class ObserverWithState {
//        Lifecycle.State mState;
//        LifecycleEventObserver mLifecycleObserver;
//
//        ObserverWithState(LifecycleObserver observer, Lifecycle.State initialState) {
//            //把所有类型的LifecycleObserver都转成LifecycleEventObserver，具体逻辑在Lifecycling实现
//            mLifecycleObserver = Lifecycling.lifecycleEventObserver(observer);
//            mState = initialState;
//        }
//
//        void dispatchEvent(LifecycleOwner owner, Lifecycle.Event event) {
//            Lifecycle.State newState = event.getTargetState();
//            mState = min(mState, newState);
//            //通过LifecycleEventObserver的onStateChanged方法，实现事件的分发
//            mLifecycleObserver.onStateChanged(owner, event);
//            mState = newState;
//        }
//    }
//
//    static LifecycleEventObserver lifecycleEventObserver(Object object) {
//        //先判断观察者是不是LifecycleEventObserver或FullLifecycleObserver的子类，
//        //如果是FullLifecycleObserver的子类，返回FullLifecycleObserverAdapter适配器，
//        //适配器会在onStateChanged的时候，根据不同的事件，调用他相应的方法
//        boolean isLifecycleEventObserver = object instanceof LifecycleEventObserver;
//        boolean isFullLifecycleObserver = object instanceof FullLifecycleObserver;
//        if (isLifecycleEventObserver && isFullLifecycleObserver) {
//            return new FullLifecycleObserverAdapter((FullLifecycleObserver) object,
//                    (LifecycleEventObserver) object);
//        }
//        if (isFullLifecycleObserver) {
//            return new FullLifecycleObserverAdapter((FullLifecycleObserver) object, null);
//        }
//
//        //如果只是LifecycleEventObserver的子类，直接返回
//        if (isLifecycleEventObserver) {
//            return (LifecycleEventObserver) object;
//        }
//
//        //如果观察者是通过实现LifecycleObserver，并用注解 @OnLifecycleEvent修饰方法这样的方式实现的，
//        //就走下面的逻辑，会用反射实现，具体就不展开了
//        //因为会用到反射，所以会有性能问题，这就是这种方法不推荐使用的原因
//        final Class<?> klass = object.getClass();
//        int type = getObserverConstructorType(klass);
//        if (type == GENERATED_CALLBACK) {
//            List<Constructor<? extends GeneratedAdapter>> constructors =
//                    sClassToAdapters.get(klass);
//            if (constructors.size() == 1) {
//                GeneratedAdapter generatedAdapter = createGeneratedAdapter(
//                        constructors.get(0), object);
//                return new SingleGeneratedAdapterObserver(generatedAdapter);
//            }
//            GeneratedAdapter[] adapters = new GeneratedAdapter[constructors.size()];
//            for (int i = 0; i < constructors.size(); i++) {
//                adapters[i] = createGeneratedAdapter(constructors.get(i), object);
//            }
//            return new CompositeGeneratedAdaptersObserver(adapters);
//        }
//        return new ReflectiveGenericLifecycleObserver(object);
//    }
//}
//
