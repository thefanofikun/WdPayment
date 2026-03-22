<script setup>
import { computed, reactive, ref, onMounted } from "vue";

const gatewayOperation = ref("onboarding");
const selectedChannelCode = ref("");
const channels = ref([]);
const payoutTypes = ref([]);
const gatewayResponse = ref(null);
const gatewayError = ref("");
const gatewaySubmitting = ref(false);

const gatewayEndpointMap = {
  onboarding: "/api/gateway/customers/onboarding",
  virtualAccount: "/api/gateway/virtual-accounts",
  beneficiary: "/api/gateway/beneficiaries",
  payout: "/api/gateway/payouts",
  webhook: "/api/gateway/webhooks/ingest"
};

const gatewayOperationMeta = {
  onboarding: { label: "Customer Onboarding", operation: "CUSTOMER_ONBOARDING" },
  virtualAccount: { label: "Create VA", operation: "VIRTUAL_ACCOUNT" },
  beneficiary: { label: "Create Beneficiary", operation: "BENEFICIARY" },
  payout: { label: "Create Payout", operation: "PAYOUT" },
  webhook: { label: "Webhook Ingest", operation: "WEBHOOK" }
};

const forms = reactive({
  onboarding: {
    merchantId: "MERCHANT-001",
    customerReference: "CUS-10001",
    legalName: "Northstar Treasury Ltd",
    shortName: "Northstar",
    businessType: "LIMITED_COMPANY",
    country: "SG",
    registrationNumber: "202600188N",
    contactEmail: "ops@northstar.test",
    contactPhone: "+6588880001",
    settlementCurrency: "USD",
    relationshipManager: "Alice Wong"
  },
  virtualAccount: {
    merchantId: "MERCHANT-001",
    customerReference: "CUS-10001",
    virtualAccountReference: "VA-90001",
    accountName: "Northstar Client Funds",
    currency: "USD",
    country: "SG",
    bankCode: "7339",
    purpose: "Collections",
    masterAccountNumber: "20301000004062",
    masterAccountCurrency: "USD",
    createCount: 1,
    externalRequestId: "VA-REQ-001"
  },
  beneficiary: {
    merchantId: "MERCHANT-001",
    customerReference: "CUS-10001",
    beneficiaryReference: "BEN-31001",
    beneficiaryName: "Oceanic Supplies Pte Ltd",
    beneficiaryType: "CORPORATE",
    bankCountry: "SG",
    bankCode: "7339",
    accountNumber: "1234567890",
    iban: "",
    swiftCode: "OCBCSGSG",
    currency: "USD"
  },
  payout: {
    merchantId: "MERCHANT-001",
    payoutReference: "PO-77001",
    payoutType: "EXTERNAL_PAYOUT",
    amount: "2500.00",
    currency: "USD",
    sourceAccountReference: "VA-90001",
    beneficiaryReference: "BEN-31001",
    narrative: "Vendor settlement",
    purposeCode: "SUPPLIER",
    valueDate: "2026-03-22",
    sourceAccountNumber: "20301000014014",
    sourceAccountName: "Northstar Settlement",
    ultimateSourceAccountNumber: "79401400000031",
    beneficiaryAccountNumber: "DE12100100101234567895",
    beneficiaryAccountName: "Jane Smith",
    beneficiaryBankCountry: "DE",
    beneficiaryBankCode: "DEUTDEFF",
    beneficiarySwiftCode: "DEUTDEFF",
    beneficiaryBankName: "Deutsche Bank",
    beneficiaryAddress: "Taunusanlage 12, Frankfurt, Germany",
    beneficiaryCity: "FRANKFURT",
    chargeBearer: "OUR",
    feeCurrency: "USD"
  },
  webhook: {
    eventId: "EVT-50001",
    eventType: "PAYOUT_COMPLETED",
    signature: "mock-signature",
    payloadText: `{
  "paymentId": "PO-77001",
  "status": "COMPLETED",
  "completedAt": "2026-03-21T12:00:00Z"
}`
  }
});

const selectedChannel = computed(() =>
  channels.value.find((channel) => channel.code === selectedChannelCode.value) ?? null
);

const requestPreview = computed(() => {
  try {
    return JSON.stringify(buildPayload(gatewayOperation.value), null, 2);
  } catch (error) {
    return `Payload error: ${error.message}`;
  }
});

onMounted(loadCatalog);

async function requestJson(url, options = {}) {
  const response = await fetch(url, options);
  const body = await response.json();
  if (!response.ok || !body.success) {
    throw new Error(body.message || "Request failed");
  }
  return body.data;
}

async function loadCatalog() {
  const data = await requestJson("/api/catalog/channels");
  channels.value = data.channels ?? [];
  payoutTypes.value = data.payoutTypes ?? [];
  if (!selectedChannelCode.value && channels.value.length > 0) {
    selectedChannelCode.value = channels.value[0].code;
  }
}

function buildPayload(operationKey) {
  if (!selectedChannelCode.value) {
    throw new Error("Select a channel first");
  }

  if (operationKey === "onboarding") return { channelCode: selectedChannelCode.value, ...forms.onboarding };
  if (operationKey === "virtualAccount") {
    return {
      channelCode: selectedChannelCode.value,
      ...forms.virtualAccount,
      createCount: Number(forms.virtualAccount.createCount)
    };
  }
  if (operationKey === "beneficiary") return { channelCode: selectedChannelCode.value, ...forms.beneficiary };
  if (operationKey === "payout") {
    return { channelCode: selectedChannelCode.value, ...forms.payout, amount: Number(forms.payout.amount) };
  }

  return {
    channelCode: selectedChannelCode.value,
    eventId: forms.webhook.eventId,
    eventType: forms.webhook.eventType,
    signature: forms.webhook.signature,
    payload: JSON.parse(forms.webhook.payloadText || "{}")
  };
}

async function submitGatewayOperation() {
  gatewaySubmitting.value = true;
  gatewayError.value = "";
  gatewayResponse.value = null;

  try {
    const payload = buildPayload(gatewayOperation.value);
    gatewayResponse.value = await requestJson(gatewayEndpointMap[gatewayOperation.value], {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
  } catch (error) {
    gatewayError.value = error.message;
  } finally {
    gatewaySubmitting.value = false;
  }
}
</script>

<template>
  <section class="workspace-grid">
    <aside class="card side-panel">
      <label class="field-label">Selected Channel</label>
      <select v-model="selectedChannelCode" class="input">
        <option disabled value="">Choose channel</option>
        <option v-for="channel in channels" :key="channel.code" :value="channel.code">
          {{ channel.code }} · {{ channel.name }}
        </option>
      </select>
      <p v-if="selectedChannel" class="muted small">{{ selectedChannel.notes }}</p>

      <div class="stack">
        <button
          v-for="(meta, key) in gatewayOperationMeta"
          :key="key"
          type="button"
          class="menu-button"
          :class="{ active: gatewayOperation === key }"
          @click="gatewayOperation = key"
        >
          {{ meta.label }}
        </button>
      </div>
    </aside>

    <main class="content-stack">
      <section class="card content-card">
        <div class="section-header">
          <div>
            <p class="eyebrow">Unified Upstream API</p>
            <h2>{{ gatewayOperationMeta[gatewayOperation].label }}</h2>
          </div>
          <button type="button" class="primary-button" :disabled="gatewaySubmitting" @click="submitGatewayOperation">
            {{ gatewaySubmitting ? "Submitting..." : "Send Request" }}
          </button>
        </div>

        <div v-if="gatewayOperation === 'onboarding'" class="form-grid">
          <label><span class="field-label">Merchant ID</span><input v-model="forms.onboarding.merchantId" class="input" /></label>
          <label><span class="field-label">Customer Reference</span><input v-model="forms.onboarding.customerReference" class="input" /></label>
          <label><span class="field-label">Legal Name</span><input v-model="forms.onboarding.legalName" class="input" /></label>
          <label><span class="field-label">Short Name</span><input v-model="forms.onboarding.shortName" class="input" /></label>
          <label><span class="field-label">Business Type</span><input v-model="forms.onboarding.businessType" class="input" /></label>
          <label><span class="field-label">Country</span><input v-model="forms.onboarding.country" class="input" /></label>
          <label><span class="field-label">Registration Number</span><input v-model="forms.onboarding.registrationNumber" class="input" /></label>
          <label><span class="field-label">Contact Email</span><input v-model="forms.onboarding.contactEmail" class="input" /></label>
          <label><span class="field-label">Contact Phone</span><input v-model="forms.onboarding.contactPhone" class="input" /></label>
          <label><span class="field-label">Settlement Currency</span><input v-model="forms.onboarding.settlementCurrency" class="input" /></label>
          <label class="span-2"><span class="field-label">Relationship Manager</span><input v-model="forms.onboarding.relationshipManager" class="input" /></label>
        </div>

        <div v-if="gatewayOperation === 'virtualAccount'" class="form-grid">
          <label><span class="field-label">Merchant ID</span><input v-model="forms.virtualAccount.merchantId" class="input" /></label>
          <label><span class="field-label">Customer Reference</span><input v-model="forms.virtualAccount.customerReference" class="input" /></label>
          <label><span class="field-label">VA Reference</span><input v-model="forms.virtualAccount.virtualAccountReference" class="input" /></label>
          <label><span class="field-label">Account Name</span><input v-model="forms.virtualAccount.accountName" class="input" /></label>
          <label><span class="field-label">Currency</span><input v-model="forms.virtualAccount.currency" class="input" /></label>
          <label><span class="field-label">Country</span><input v-model="forms.virtualAccount.country" class="input" /></label>
          <label><span class="field-label">Bank Code</span><input v-model="forms.virtualAccount.bankCode" class="input" /></label>
          <label><span class="field-label">Master Account No</span><input v-model="forms.virtualAccount.masterAccountNumber" class="input" /></label>
          <label><span class="field-label">Master Account CCY</span><input v-model="forms.virtualAccount.masterAccountCurrency" class="input" /></label>
          <label><span class="field-label">Create Count</span><input v-model="forms.virtualAccount.createCount" type="number" min="1" class="input" /></label>
          <label><span class="field-label">External Request ID</span><input v-model="forms.virtualAccount.externalRequestId" class="input" /></label>
          <label class="span-2"><span class="field-label">Purpose</span><input v-model="forms.virtualAccount.purpose" class="input" /></label>
        </div>

        <div v-if="gatewayOperation === 'beneficiary'" class="form-grid">
          <label><span class="field-label">Merchant ID</span><input v-model="forms.beneficiary.merchantId" class="input" /></label>
          <label><span class="field-label">Customer Reference</span><input v-model="forms.beneficiary.customerReference" class="input" /></label>
          <label><span class="field-label">Beneficiary Reference</span><input v-model="forms.beneficiary.beneficiaryReference" class="input" /></label>
          <label><span class="field-label">Beneficiary Name</span><input v-model="forms.beneficiary.beneficiaryName" class="input" /></label>
          <label><span class="field-label">Beneficiary Type</span><input v-model="forms.beneficiary.beneficiaryType" class="input" /></label>
          <label><span class="field-label">Bank Country</span><input v-model="forms.beneficiary.bankCountry" class="input" /></label>
          <label><span class="field-label">Bank Code</span><input v-model="forms.beneficiary.bankCode" class="input" /></label>
          <label><span class="field-label">Account Number</span><input v-model="forms.beneficiary.accountNumber" class="input" /></label>
          <label><span class="field-label">IBAN</span><input v-model="forms.beneficiary.iban" class="input" /></label>
          <label><span class="field-label">SWIFT</span><input v-model="forms.beneficiary.swiftCode" class="input" /></label>
          <label class="span-2"><span class="field-label">Currency</span><input v-model="forms.beneficiary.currency" class="input" /></label>
        </div>

        <div v-if="gatewayOperation === 'payout'" class="form-grid">
          <label><span class="field-label">Merchant ID</span><input v-model="forms.payout.merchantId" class="input" /></label>
          <label><span class="field-label">Payout Reference</span><input v-model="forms.payout.payoutReference" class="input" /></label>
          <label><span class="field-label">Payout Type</span><select v-model="forms.payout.payoutType" class="input"><option v-for="type in payoutTypes" :key="type" :value="type">{{ type }}</option></select></label>
          <label><span class="field-label">Amount</span><input v-model="forms.payout.amount" class="input" type="number" min="0" step="0.01" /></label>
          <label><span class="field-label">Currency</span><input v-model="forms.payout.currency" class="input" /></label>
          <label><span class="field-label">Source Account</span><input v-model="forms.payout.sourceAccountReference" class="input" /></label>
          <label><span class="field-label">Beneficiary</span><input v-model="forms.payout.beneficiaryReference" class="input" /></label>
          <label><span class="field-label">Source Account No</span><input v-model="forms.payout.sourceAccountNumber" class="input" /></label>
          <label><span class="field-label">Source Account Name</span><input v-model="forms.payout.sourceAccountName" class="input" /></label>
          <label><span class="field-label">Ultimate Source Acct</span><input v-model="forms.payout.ultimateSourceAccountNumber" class="input" /></label>
          <label><span class="field-label">Beneficiary Acct No</span><input v-model="forms.payout.beneficiaryAccountNumber" class="input" /></label>
          <label><span class="field-label">Beneficiary Name</span><input v-model="forms.payout.beneficiaryAccountName" class="input" /></label>
          <label><span class="field-label">Beneficiary Country</span><input v-model="forms.payout.beneficiaryBankCountry" class="input" /></label>
          <label><span class="field-label">Beneficiary Bank Code</span><input v-model="forms.payout.beneficiaryBankCode" class="input" /></label>
          <label><span class="field-label">Beneficiary SWIFT/BIC</span><input v-model="forms.payout.beneficiarySwiftCode" class="input" /></label>
          <label><span class="field-label">Beneficiary Bank Name</span><input v-model="forms.payout.beneficiaryBankName" class="input" /></label>
          <label><span class="field-label">Purpose Code</span><input v-model="forms.payout.purposeCode" class="input" /></label>
          <label><span class="field-label">Charge Bearer</span><input v-model="forms.payout.chargeBearer" class="input" /></label>
          <label><span class="field-label">Fee Currency</span><input v-model="forms.payout.feeCurrency" class="input" /></label>
          <label><span class="field-label">Value Date</span><input v-model="forms.payout.valueDate" type="date" class="input" /></label>
          <label class="span-2"><span class="field-label">Beneficiary Address</span><input v-model="forms.payout.beneficiaryAddress" class="input" /></label>
          <label><span class="field-label">Beneficiary City</span><input v-model="forms.payout.beneficiaryCity" class="input" /></label>
          <label class="span-2"><span class="field-label">Narrative</span><input v-model="forms.payout.narrative" class="input" /></label>
        </div>

        <div v-if="gatewayOperation === 'webhook'" class="form-grid">
          <label><span class="field-label">Event ID</span><input v-model="forms.webhook.eventId" class="input" /></label>
          <label><span class="field-label">Event Type</span><input v-model="forms.webhook.eventType" class="input" /></label>
          <label class="span-2"><span class="field-label">Signature</span><input v-model="forms.webhook.signature" class="input" /></label>
          <label class="span-2"><span class="field-label">Payload JSON</span><textarea v-model="forms.webhook.payloadText" class="input textarea"></textarea></label>
        </div>

        <p v-if="gatewayError" class="error-text">{{ gatewayError }}</p>
      </section>
    </main>

    <aside class="card inspector-panel">
      <p class="eyebrow">Request Preview</p>
      <pre>{{ requestPreview }}</pre>
      <div class="divider"></div>
      <p class="eyebrow">Gateway Response</p>
      <pre>{{ gatewayResponse ? JSON.stringify(gatewayResponse, null, 2) : "Waiting for gateway call..." }}</pre>
    </aside>
  </section>
</template>
