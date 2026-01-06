package br.com.fivecom.litoralfm.ui.spinners;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import br.com.fivecom.litoralfm.R;

public class SearchableSpinner extends androidx.appcompat.widget.AppCompatSpinner implements View.OnTouchListener,
        SearchableListDialog.SearchableItem {

    public static final int NO_ITEM_SELECTED = -1;
    private Context _context;
    private List _items;
    private SearchableListDialog _searchableListDialog;

    private boolean _isDirty;
    private ArrayAdapter _arrayAdapter;
    private String _strHintText;
    private boolean _isFromInit;

    public SearchableSpinner(Context context) {
        super(context);
        this._context = context;
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this._context = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SearchableSpinner);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.SearchableSpinner_hintText) {
                _strHintText = a.getString(attr);
            }
        }
        a.recycle();
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this._context = context;
        init();
    }

    private void init() {
        _items = new ArrayList();
        _searchableListDialog = SearchableListDialog.newInstance(_items);
        _searchableListDialog.setOnSearchableItemClickListener(this);
        setOnTouchListener(this);

        _arrayAdapter = (ArrayAdapter) getAdapter();
        if (!TextUtils.isEmpty(_strHintText)) {
            ArrayAdapter arrayAdapter = new ArrayAdapter(_context, R.layout.item_spinner,
                    new String[] { _strHintText });
            _isFromInit = true;
            setAdapter(arrayAdapter);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Open the native spinner dropdown (collapsible list) instead of the searchable
            // dialog
            performClick();
        }
        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {

        if (!_isFromInit) {
            _arrayAdapter = (ArrayAdapter) adapter;
            if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
                ArrayAdapter arrayAdapter = new ArrayAdapter(_context, android.R.layout.simple_list_item_1,
                        new String[] { _strHintText });
                super.setAdapter(arrayAdapter);
            } else {
                super.setAdapter(adapter);
            }

        } else {
            _isFromInit = false;
            super.setAdapter(adapter);
        }
    }

    @Override
    public void onSearchableItemClicked(Object item, int position) {
        setSelection(_items.indexOf(item));
        if (!_isDirty) {
            _isDirty = true;
            setAdapter(_arrayAdapter);
            setSelection(_items.indexOf(item));
        }
    }

    public void setTitle(String strTitle) {
        _searchableListDialog.setTitle(strTitle);
    }

    public void setDDI(boolean isDDI) {
        _searchableListDialog.setDDI(isDDI);
    }

    public void setPositiveButton(String strPositiveButtonText) {
        _searchableListDialog.setPositiveButton(strPositiveButtonText);
    }

    public void setPositiveButton(String strPositiveButtonText, DialogInterface.OnClickListener onClickListener) {
        _searchableListDialog.setPositiveButton(strPositiveButtonText, onClickListener);
    }

    public void setOnSearchTextChangedListener(SearchableListDialog.OnSearchTextChanged onSearchTextChanged) {
        _searchableListDialog.setOnSearchTextChangedListener(onSearchTextChanged);
    }

    /**
     * Programmatically show the searchable dialog (same action performed on touch).
     */
    public void showSearchableDialog() {
        if (_searchableListDialog != null && !_searchableListDialog.isAdded()) {
            _items.clear();
            if (_arrayAdapter != null) {
                for (int i = 0; i < _arrayAdapter.getCount(); i++) {
                    _items.add(_arrayAdapter.getItem(i));
                }
            }
            Activity act = scanForActivity(_context);
            if (act != null) {
                _searchableListDialog.show(act.getFragmentManager(), "TAG");
            }
        }
    }

    /**
     * Selects an item by index and forces the spinner to use the real adapter (not
     * the hint adapter).
     * Safe to call after setAdapter(...) to show a default selection
     * programmatically.
     */
    public void selectItemByIndex(int index) {
        if (_arrayAdapter == null)
            return;
        // Mark as dirty so the real adapter is used
        _isDirty = true;
        // Set the real adapter and set selection
        super.setAdapter(_arrayAdapter);
        if (index >= 0 && index < _arrayAdapter.getCount()) {
            setSelection(index);
        }
    }

    private Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity) cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) cont).getBaseContext());

        return null;
    }

    @Override
    public int getSelectedItemPosition() {
        if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            return NO_ITEM_SELECTED;
        } else {
            return super.getSelectedItemPosition();
        }
    }

    @Override
    public Object getSelectedItem() {
        if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            return null;
        } else {
            return super.getSelectedItem();
        }
    }
}
