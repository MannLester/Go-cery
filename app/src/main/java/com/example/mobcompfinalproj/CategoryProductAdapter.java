package com.example.mobcompfinalproj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryProductAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> categories;
    private Map<String, List<Product>> categoryProducts;
    private OnProductUpdateListener listener;

    public interface OnProductUpdateListener {
        void onQuantityChanged();
        void onProductRemoved(Product product);
    }

    public CategoryProductAdapter(Context context, List<Product> products, OnProductUpdateListener listener) {
        this.context = context;
        this.listener = listener;
        this.categories = new ArrayList<>();
        this.categoryProducts = new HashMap<>();

        // Group products by category
        for (Product product : products) {
            String category = product.getCategory();
            if (!categories.contains(category)) {
                categories.add(category);
                categoryProducts.put(category, new ArrayList<>());
            }
            categoryProducts.get(category).add(product);
        }
    }

    @Override
    public int getGroupCount() {
        return categories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return categoryProducts.get(categories.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categories.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return categoryProducts.get(categories.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_group, parent, false);
        }

        TextView categoryText = convertView.findViewById(R.id.categoryText);
        categoryText.setText(categories.get(groupPosition));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_child, parent, false);
        }

        Product product = (Product) getChild(groupPosition, childPosition);

        TextView productNameText = convertView.findViewById(R.id.productNameText);
        TextView priceText = convertView.findViewById(R.id.priceText);
        TextView quantityText = convertView.findViewById(R.id.quantityText);
        Button decreaseButton = convertView.findViewById(R.id.decreaseButton);
        Button increaseButton = convertView.findViewById(R.id.increaseButton);
        Button removeButton = convertView.findViewById(R.id.removeButton);

        productNameText.setText(product.getProductName());
        priceText.setText(String.format("$%.2f", product.getPriceAsDouble()));
        quantityText.setText(String.valueOf(product.getSelectedQuantity()));

        decreaseButton.setOnClickListener(v -> {
            if (product.getSelectedQuantity() > 0) {
                product.setSelectedQuantity(product.getSelectedQuantity() - 1);
                quantityText.setText(String.valueOf(product.getSelectedQuantity()));
                listener.onQuantityChanged();
            }
        });

        increaseButton.setOnClickListener(v -> {
            if (product.getSelectedQuantity() < product.getInventoryCount()) {
                product.setSelectedQuantity(product.getSelectedQuantity() + 1);
                quantityText.setText(String.valueOf(product.getSelectedQuantity()));
                listener.onQuantityChanged();
            } else {
                Toast.makeText(context, "Cannot exceed available inventory", Toast.LENGTH_SHORT).show();
            }
        });

        removeButton.setOnClickListener(v -> {
            listener.onProductRemoved(product);
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void updateProducts(List<Product> products) {
        categories.clear();
        categoryProducts.clear();

        for (Product product : products) {
            String category = product.getCategory();
            if (!categories.contains(category)) {
                categories.add(category);
                categoryProducts.put(category, new ArrayList<>());
            }
            categoryProducts.get(category).add(product);
        }
        notifyDataSetChanged();
    }
} 