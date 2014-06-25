/*
 * Copyright (c) 2014 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.developers.actions.debugger;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.developers.actions.debugger.util.ActionData;

import java.util.List;

public class ActionsListFragment extends Fragment {

    private Callbacks mCallbacks;
    private GridView mGridView;

    public ActionsListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View listView = inflater.inflate(R.layout.list_fragment, container, false);
        mGridView = (GridView) listView.findViewById(R.id.grid_view);
        TextView emptyView = (TextView) listView.findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyView);
        return listView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) getView().findViewById(R.id.display_name)).setText(getString(R.string.poweredBy));
        ((ImageView) getView().findViewById(R.id.avatar)).setImageResource(R.drawable.cayley_transparent);
    }

    public void setActions(List<ActionData> actions) {
        if (!isAdded()) {
            return;
        }

        mGridView.setAdapter(new ActionAdapter(actions));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new ClassCastException("Activity must implement callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public interface Callbacks {

        public void onActionSelected(ActionData action);
    }

    private class ActionAdapter extends BaseAdapter {
        private final List<ActionData> mActions;

        private ActionAdapter(List<ActionData> actions) {
            mActions = actions;
        }

        @Override
        public int getCount() {
            return mActions.size();
        }

        @Override
        public Object getItem(int i) {
            return mActions.get(i);
        }

        @Override
        public long getItemId(int i) {
            return mActions.get(i).getAction().hashCode();
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.list_item, container, false);
            }

            ActionData action = mActions.get(position);
            ((ImageView) convertView.findViewById(R.id.thumbnail)).setImageResource(action.getThumbnail(getActivity()));

            convertView.findViewById(R.id.main_target).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCallbacks.onActionSelected(mActions.get(position));
                        }
                    }
            );
            return convertView;
        }
    }
}
