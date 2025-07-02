/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
 */
package it.wldt.core.event.observer;

import it.wldt.core.event.WldtEvent;

/**
 * Interface for the WldtEventObserverListener allowing to listen to the events of the WldtEventObserver
 */
public interface IWldtEventObserverListener {

    public void onEventSubscribed(String eventType);

    public void onEventUnSubscribed(String eventType);

    public void onStateEvent(WldtEvent<?> wldtEvent);

    public void onPhysicalAssetEvent(WldtEvent<?> wldtEvent);

    public void onPhysicalAssetActionEvent(WldtEvent<?> wldtEvent);

    public void onDigitalActionEvent(WldtEvent<?> wldtEvent);

    public void onPhysicalAssetDescriptionEvent(WldtEvent<?> wldtEvent);

    public void onLifeCycleEvent(WldtEvent<?> wldtEvent);

    public void onQueryRequestEvent(WldtEvent<?> wldtEvent);

    public void onQueryResultEvent(WldtEvent<?> wldtEvent);
}
