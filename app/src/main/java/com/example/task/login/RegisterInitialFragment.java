package com.example.task.login;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.task.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

/**
 * This class displays the initial form of the user registration.
 */
public class RegisterInitialFragment extends Fragment {

    // Initialize member variables
    private ConstraintLayout mParentLayout;
    private TextInputLayout mNameLayout;
    private TextInputLayout mPhoneLayout;
    private TextInputEditText mEditNameView;
    private TextInputEditText mEditPhoneView;
    private Button mNextButton;
    private CheckBox mAgreeCheckBox;

    // Declared variables
    private String name;
    private String phone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_initial, container, false);

        // Capture the view objects from the inflated layout.
        mParentLayout = view.findViewById(R.id.constraintLayout_initial_register);
        mNameLayout = view.findViewById(R.id.inputLayout_full_name);
        mPhoneLayout = view.findViewById(R.id.inputLayout_phone_number);
        mEditNameView = view.findViewById(R.id.editText_full_name);
        mEditPhoneView = view.findViewById(R.id.editText_phone_number);
        mNextButton = view.findViewById(R.id.button_next);
        mAgreeCheckBox = view.findViewById(R.id.checkBox_agree);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigation controller setup
        final NavController navController = Navigation.findNavController(requireActivity(),
                R.id.login_nav_host);

        // This button will navigate user to the final registration, if conditions are met.
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validate the name and phone number fields before proceeding.
                if (!validateName() | !validateNumber()) {
                    return;
                }

                // Get the user inputs and pass it to the navigation action.
                name = Objects.requireNonNull(mEditNameView.getText()).toString();
                phone = Objects.requireNonNull(mEditPhoneView.getText()).toString();

                // Check is the user agrees to the privacy policy and agreement.
                if (mAgreeCheckBox.isChecked()) {
                    RegisterInitialFragmentDirections.ActionRegisterInitialToRegisterFinal
                            action = RegisterInitialFragmentDirections
                            .actionRegisterInitialToRegisterFinal(name, phone); // Action arguments

                    // Navigate user to the final registration.
                    navController.navigate(action);
                } else {
                    Snackbar.make(mParentLayout, R.string.tick_box, Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

    /**
     * This method will validate the name field.
     */
    private boolean validateName() {
        mEditNameView.addTextChangedListener(textWatcher);

        name = Objects.requireNonNull(mEditNameView.getText()).toString();
        if (name.isEmpty()) {
            mNameLayout.setError(getString(R.string.cannot_be_empty));
            return false;
        } else {
            mNameLayout.setError(null);
            return true;
        }
    }

    /**
     * This method will validate the phone number field.
     */
    private boolean validateNumber() {
        mEditPhoneView.addTextChangedListener(textWatcher);

        phone = Objects.requireNonNull(mEditPhoneView.getText()).toString();
        if (phone.isEmpty()) {
            mPhoneLayout.setError(getString(R.string.cannot_be_empty));
            return false;
        } else if (phone.length() < 11) {
            mPhoneLayout.setError(getString(R.string.digits_required));
            return false;
        } else {
            mPhoneLayout.setError(null);
            return true;
        }
    }

    /**
     * This object will notify the view object, in this case the EditText views
     * if there are changes in the inserted characters.
     */
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Ignored
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Remove error in the input fields.
            name = Objects.requireNonNull(mEditNameView.getText()).toString();
            if (name.length() > 0) {
                mNameLayout.setErrorEnabled(false);
            }

            phone = Objects.requireNonNull(mEditPhoneView.getText()).toString();
            if (phone.length() > 0) {
                mPhoneLayout.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Ignored
        }
    };
}