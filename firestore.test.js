const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read groups or expenses", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("groups").get());
  await assertFails(unauthDb.collection("expenses").get());
});

test("Authenticated user cannot read group they are not a member of", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("groups").doc("group_1").set({
      id: "group_1",
      name: "Trip to Goa",
      createdBy: BOB_UID,
      memberIds: [BOB_UID],
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("groups").doc("group_1").get());
});

test("Group member can read group and its expenses", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("groups").doc("group_1").set({
      id: "group_1",
      name: "Trip to Goa",
      createdBy: BOB_UID,
      memberIds: [BOB_UID, ALICE_UID],
      createdAt: new Date(),
      updatedAt: new Date(),
    });

    await context.firestore().collection("expenses").doc("exp_1").set({
      id: "exp_1",
      groupId: "group_1",
      titleNote: "Dinner",
      totalAmount: 1500,
      paidByUserId: BOB_UID,
      participantIds: [BOB_UID, ALICE_UID],
      memberIds: [BOB_UID, ALICE_UID],
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("groups").doc("group_1").get());
  await assertSucceeds(aliceDb.collection("expenses").doc("exp_1").get());
});

test("Member can create group and expense", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("groups").doc("group_alice").set({
      id: "group_alice",
      name: "Apartment Rent",
      createdBy: ALICE_UID,
      memberIds: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("expenses").doc("exp_alice").set({
      id: "exp_alice",
      groupId: "group_alice",
      titleNote: "WiFi Bill",
      totalAmount: 999,
      paidByUserId: ALICE_UID,
      participantIds: [ALICE_UID, BOB_UID],
      memberIds: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});
