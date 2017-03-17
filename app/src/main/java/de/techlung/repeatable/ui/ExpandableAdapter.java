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

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;

import de.techlung.repeatable.Constants;
import de.techlung.repeatable.DataManager;
import de.techlung.repeatable.R;
import de.techlung.repeatable.model.Category;
import de.techlung.repeatable.model.Item;
import de.techlung.repeatable.widget.WidgetProvider;

public class ExpandableAdapter
        extends AbstractExpandableItemAdapter<ExpandableAdapter.MyGroupViewHolder, ExpandableAdapter.MyChildViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_ITEM_CONTROL = 1;

    private static final String TAG = "MyExpandableItemAdapter";
    private AbstractExpandableDataProvider provider;
    private Context context;

    public ExpandableAdapter(Context context, AbstractExpandableDataProvider dataProvider) {
        this.provider = dataProvider;
        this.context = context;

        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
    }

    @Override
    public int getGroupCount() {
        return provider.getGroupCount();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return provider.getChildCount(groupPosition) + 1;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return provider.getGroupItem(groupPosition).getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {

        if (childPosition == provider.getChildCount(groupPosition)) {
            return groupPosition + 2000; // Item Controll element
        } else {
            return provider.getChildItem(groupPosition, childPosition).getId();
        }

    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        return 0;
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        if (childPosition == provider.getChildCount(groupPosition)) {
            return VIEW_TYPE_ITEM_CONTROL;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.repeatable_list_category, parent, false);
        return new MyGroupViewHolder(v);
    }

    @Override
    public MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_ITEM) {
            final View v = inflater.inflate(R.layout.repeatable_list_item, parent, false);
            return new MyChildItemViewHolder(v);
        } else {
            final View v = inflater.inflate(R.layout.repeatable_list_item_controller, parent, false);
            return new MyChildControllerViewHolder(v);
        }

    }

    @Override
    public void onBindGroupViewHolder(MyGroupViewHolder holder, final int groupPosition, int viewType) {
        // child item
        final Category category = provider.getGroupItem(groupPosition);

        holder.text.setText(category.getName());
        holder.colorIndicator.setBackgroundResource(Constants.COLOR_RESOURCE_IDS[category.getColorIndex()]);

        holder.itemView.setClickable(true);
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView.ViewHolder vh = RecyclerViewAdapterUtils.getViewHolder(v);
                int flatPosition = vh.getAdapterPosition();

                if (flatPosition == RecyclerView.NO_POSITION) {
                    return;
                }

                DataManager.createOrEditCategory(context, category, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            provider.editedGroup(groupPosition);
                        } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                            provider.removeGroup(groupPosition);
                        }
                    }
                });
            }
        });

        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataManager.createOrEditItem(context, category, null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            provider.addChildItem(groupPosition, category.getItems().size() - 1);
                        }
                    }
                });
            }
        });

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOnClickGroupItemContainerView(groupPosition);
            }
        });

        // set background resource (target view ID: container)
        final int expandState = holder.getExpandStateFlags();

        int bgResId;
        int openIcon;

        if ((expandState & Expandable.STATE_FLAG_IS_EXPANDED) != 0) {
            bgResId = Constants.COLOR_RESOURCE_IDS[category.getColorIndex()];
            openIcon = R.drawable.ic_keyboard_arrow_up_black_24dp;
        } else {
            bgResId = android.R.color.white;
            openIcon = R.drawable.ic_keyboard_arrow_down_black_24dp;
        }

        holder.container.setBackgroundResource(bgResId);
        holder.openIcon.setImageResource(openIcon);
    }

    @Override
    public void onBindChildViewHolder(final MyChildViewHolder holderGeneric, final int groupPosition, final int childPosition, int viewType) {
        final Category category = provider.getGroupItem(groupPosition);

        if (viewType == VIEW_TYPE_ITEM) {
            final MyChildItemViewHolder holder = (MyChildItemViewHolder) holderGeneric;

            // group item
            final Item item = provider.getChildItem(groupPosition, childPosition);

            holder.colorIndicator.setBackgroundResource(Constants.COLOR_RESOURCE_IDS[category.getColorIndex()]);

            // set text
            holder.name.setText(item.getName());
            holder.checkBox.setChecked(item.getChecked());

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataManager.toggleCheck(item);
                    holder.checkBox.setChecked(item.getChecked());

                    WidgetProvider.reloadWidgets(context);
                }
            };

            holder.checkBox.setOnClickListener(clickListener);
            holder.container.setOnClickListener(clickListener);

            // set background resource (target view ID: container)
            int bgResId;
            bgResId = android.R.color.white;
            holder.container.setBackgroundResource(bgResId);

            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataManager.createOrEditItem(context, provider.getGroupItem(groupPosition), item, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEUTRAL) {
                                provider.removeChildItem(groupPosition, childPosition);
                            } else if (which == DialogInterface.BUTTON_POSITIVE) {
                                provider.editedChild(groupPosition, childPosition);
                            }
                        }
                    });
                }
            });
        } else {
            final MyChildControllerViewHolder holder = (MyChildControllerViewHolder) holderGeneric;

            holder.container.setBackgroundResource(Constants.COLOR_RESOURCE_IDS[category.getColorIndex()]);

            holder.selectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataManager.deselectAllItemsOfCategory(category.getId());
                    notifyAllChildrenEdited(groupPosition);
                }
            });

            holder.save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataManager.saveItemStateOfCategory(category.getId());
                    Toast.makeText(context, R.string.item_controller_save_state_toast, Toast.LENGTH_SHORT).show();
                    notifyAllChildrenEdited(groupPosition);
                }
            });

            holder.load.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataManager.loadItemStateOfCategory(category.getId());
                    Toast.makeText(context, R.string.item_controller_load_state_toast, Toast.LENGTH_SHORT).show();
                    notifyAllChildrenEdited(groupPosition);
                }
            });
        }
    }

    private void notifyAllChildrenEdited(int groupPosition) {
        for (int i = 0; i < provider.getChildCount(groupPosition); ++i) {
            provider.editedChild(groupPosition, i);
        }
    }

    private void handleOnClickGroupItemContainerView(int groupPosition) {
        // toggle expanded/collapsed
        if (isGroupExpanded(groupPosition)) {
            provider.collapseGroup(groupPosition);
        } else {
            provider.expandGroup(groupPosition);
        }
    }

    private boolean isGroupExpanded(int groupPosition) {
        return provider.isGroupExpanded(groupPosition);
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        return false;
    }

    // NOTE: Make accessible with short name
    private interface Expandable extends ExpandableItemConstants {
    }

    static class MyGroupViewHolder extends AbstractExpandableItemViewHolder {
        RelativeLayout container;
        TextView text;
        View add;
        View edit;
        View colorIndicator;
        ImageView openIcon;

        MyGroupViewHolder(View v) {
            super(v);
            container = (RelativeLayout) v.findViewById(R.id.container);
            text = (TextView) v.findViewById(R.id.text);
            add = v.findViewById(R.id.add);
            edit = v.findViewById(R.id.edit);
            colorIndicator = v.findViewById(R.id.colorIndicator);
            openIcon = (ImageView) v.findViewById(R.id.openIcon);
        }
    }

    static class MyChildViewHolder extends AbstractExpandableItemViewHolder {

        MyChildViewHolder(View v) {
            super(v);
        }
    }

    private static class MyChildItemViewHolder extends MyChildViewHolder {
        RelativeLayout container;
        TextView name;
        View edit;
        AppCompatCheckBox checkBox;
        View colorIndicator;

        MyChildItemViewHolder(View v) {
            super(v);
            container = (RelativeLayout) v.findViewById(R.id.container);
            name = (TextView) v.findViewById(R.id.name);
            edit = v.findViewById(R.id.edit);
            checkBox = (AppCompatCheckBox) v.findViewById(R.id.checked);
            colorIndicator = v.findViewById(R.id.colorIndicator);
        }
    }

    private static class MyChildControllerViewHolder extends MyChildViewHolder {
        RelativeLayout container;
        Button selectAll;
        Button save;
        Button load;

        MyChildControllerViewHolder(View v) {
            super(v);
            container = (RelativeLayout) v.findViewById(R.id.container);
            selectAll = (Button) v.findViewById(R.id.select_all);
            save = (Button) v.findViewById(R.id.save);
            load = (Button) v.findViewById(R.id.load);
        }
    }
}
