package com.erecycler.server.service;

import com.erecycler.server.common.ErrorCase;
import com.erecycler.server.domain.RecycleGuide;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;

@Service
public class RecycleGuideService {
	private static final Firestore DATABASE = FirestoreClient.getFirestore();
	private static final String COLLECTION_NAME = "guides";
	private static final String SUB_COLLECTION_NAME = "items";
	private static final String MATERIAL_FIELD_NAME = "material";
	private static final String ITEM_FIELD_NAME = "item";
	private static final String GUIDELINE_FIELD_NAME = "guideline";
	private static final String OK_FLAG = "OK";

	public String addGuide(RecycleGuide recycleGuide) {
		String material = recycleGuide.getMaterial();

		if (recycleGuide.getGuideline() == null) {
			return ErrorCase.EMPTY_GUIDELINE_ERROR;
		}
		if (!isMaterialExist(material)) {
			addMaterial(material);
		}
		Map<String, Object> data = new HashMap<>();
		data.put(ITEM_FIELD_NAME, recycleGuide.getItem());
		data.put(GUIDELINE_FIELD_NAME, recycleGuide.getGuideline());
		DATABASE.collection(COLLECTION_NAME).document(material)
			.collection(SUB_COLLECTION_NAME).document(recycleGuide.getItem())
			.set(data);
		return OK_FLAG;
	}

	public List<String> getMaterials() {
		List<String> result = new ArrayList<>();
		ApiFuture<QuerySnapshot> future = DATABASE.collection(COLLECTION_NAME).get();
		try {
			for (DocumentSnapshot document : future.get().getDocuments()) {
				result.add(document.getId());
			}
		} catch (InterruptedException | ExecutionException e) {
			return Collections.singletonList(ErrorCase.DATABASE_CONNECTION_ERROR);
		}
		return result;
	}

	public List<String> getItems(String material) {
		List<String> result = new ArrayList<>();
		if (!isMaterialExist(material)) {
			return Collections.singletonList(ErrorCase.NO_SUCH_MATERIAL_ERROR);
		}
		ApiFuture<QuerySnapshot> future = DATABASE.collection(COLLECTION_NAME).document(material)
			.collection(SUB_COLLECTION_NAME).get();
		try {
			for (DocumentSnapshot document : future.get().getDocuments()) {
				result.add(document.getId());
			}
		} catch (InterruptedException | ExecutionException e) {
			return Collections.singletonList(ErrorCase.DATABASE_CONNECTION_ERROR);
		}
		return result;
	}

	public String getGuideline(String material, String item) {
		if (!isMaterialExist(material)) {
			return ErrorCase.NO_SUCH_MATERIAL_ERROR;
		}
		ApiFuture<QuerySnapshot> future = DATABASE.collection(COLLECTION_NAME).document(material)
			.collection(SUB_COLLECTION_NAME).whereEqualTo(ITEM_FIELD_NAME, item).get();
		try {
			List<QueryDocumentSnapshot> document = future.get().getDocuments();
			if (document.isEmpty()) {
				return ErrorCase.NO_SUCH_ITEM_ERROR;
			}
			return (String) document.get(0).get(GUIDELINE_FIELD_NAME);
		} catch (InterruptedException | ExecutionException e) {
			return ErrorCase.DATABASE_CONNECTION_ERROR;
		}
	}

	private boolean isMaterialExist(String material) {
		ApiFuture<QuerySnapshot> future = DATABASE
			.collection(COLLECTION_NAME).whereEqualTo(MATERIAL_FIELD_NAME, material).get();
		try {
			return !future.get().getDocuments().isEmpty();
		} catch (InterruptedException | ExecutionException e) {
			return false;
		}
	}

	private void addMaterial(String material) {
		Map<String, Object> data = new HashMap<>();
		data.put(MATERIAL_FIELD_NAME, material);
		DATABASE.collection(COLLECTION_NAME).document(material).set(data);
		// no need code for firestore works asynchronously
	}
}