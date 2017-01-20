/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.techlung.repeatable.ui;

import de.techlung.repeatable.model.Category;
import de.techlung.repeatable.model.Item;

public abstract class AbstractExpandableDataProvider {

    public abstract int getGroupCount();

    public abstract int getChildCount(int groupPosition);

    public abstract Category getGroupItem(int groupPosition);

    public abstract Item getChildItem(int groupPosition, int childPosition);

    public abstract void removeChildItem(int groupPosition, int childPosition);

    public abstract void removeGroupItem(int groupPosition);

    public abstract void addChildItem(int groupPosition, int childPosition);

    public abstract void addGroupItem(int groupPosition);

    public abstract boolean isGroupExpanded(int groupPosition);

    public abstract void collapseGroup(int groupPosition);

    public abstract void expandGroup(int groupPosition);

    public abstract void editedGroup(int groupPosition);

    public abstract void editedChild(int groupPosition, int childPosition);

}
