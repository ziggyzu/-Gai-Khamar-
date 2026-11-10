package com.gaikhamar.app.cloud;

import android.app.Activity;

import com.gaikhamar.app.data.Repository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import java.util.Arrays;

public final class CloudAuth {
    public static final int OK = 0, WRONG = 1, EXISTS = 2, NETWORK = 3, OTHER = 4;

    public static class Result {
        public final int code;
        public final String email, name, detail;

        Result(int code, String email, String name, String detail) {
            this.code = code;
            this.email = email;
            this.name = name;
            this.detail = detail;
        }

        public boolean ok() { return code == OK; }
    }

    public interface Callback {
        void onResult(Result r);
    }

    private CloudAuth() {}

    public static String uid() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return u == null ? null : u.getUid();
    }

    public static String email() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return u == null ? null : u.getEmail();
    }

    private static Result success(FirebaseUser u) {
        String email = u == null || u.getEmail() == null ? "" : u.getEmail();
        String name = u == null || u.getDisplayName() == null ? "" : u.getDisplayName();
        return new Result(OK, email, name, "");
    }

    private static Result failure(Exception e) {
        int code = OTHER;
        if (e instanceof FirebaseNetworkException) code = NETWORK;
        else if (e instanceof FirebaseAuthUserCollisionException) code = EXISTS;
        else if (e instanceof FirebaseAuthInvalidUserException
                || e instanceof FirebaseAuthInvalidCredentialsException) code = WRONG;
        String detail = e instanceof FirebaseAuthException
                ? ((FirebaseAuthException) e).getErrorCode()
                : (e == null ? "" : e.getClass().getSimpleName());
        return new Result(code, "", "", detail);
    }

    public static void signIn(String email, String password, Callback cb) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(t ->
                cb.onResult(t.isSuccessful() ? success(t.getResult().getUser()) : failure(t.getException())));
    }

    public static void create(String email, String password, Callback cb) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(t ->
                cb.onResult(t.isSuccessful() ? success(t.getResult().getUser()) : failure(t.getException())));
    }

    /** Signs in, or creates the account when it does not exist. A taken email with a wrong password stays WRONG. */
    public static void signInOrCreate(String email, String password, Callback cb) {
        signIn(email, password, r -> {
            if (r.code == WRONG) {
                create(email, password, r2 -> cb.onResult(r2.code == EXISTS ? new Result(WRONG, "", "", r.detail) : r2));
            } else {
                cb.onResult(r);
            }
        });
    }

    /** Google sign-in through Firebase's browser flow (opens a Chrome tab, then returns to the app). */
    public static void signInGoogle(Activity activity, Callback cb) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Task<AuthResult> task = auth.getPendingAuthResult();
        if (task == null) {
            OAuthProvider.Builder provider = OAuthProvider.newBuilder("google.com");
            provider.setScopes(Arrays.asList("email", "profile"));
            provider.addCustomParameter("prompt", "select_account");
            task = auth.startActivityForSignInWithProvider(activity, provider.build());
        }
        task.addOnSuccessListener(r -> cb.onResult(success(r.getUser())))
                .addOnFailureListener(e -> cb.onResult(failure(e)));
    }

    public static void sendReset(String email, Repository.Callback<Boolean> cb) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(t -> cb.onResult(true));
    }
}