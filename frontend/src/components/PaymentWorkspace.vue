<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";

const paymentOrders = ref([]);
const paymentResponse = ref(null);
const paymentError = ref("");
const paymentSubmitting = ref(false);
const routingRows = ref([]);
const channels = ref([]);

const referenceData = reactive({
  merchants: [],
  customers: [],
  beneficiaries: [],
  sourceAccounts: []
});

const approvers = reactive({
  checkerActor: "checker.user",
  checkerComment: "Risk checklist completed",
  l1Actor: "l1.manager",
  l1Comment: "Business review passed",
  l2Actor: "l2.finance",
  l2Comment: "Final release approved",
  opsActor: "ops.user",
  opsComment: "Operations action recorded"
});

const form = reactive({
  merchantId: "MERCHANT-001",
  customerReference: "CUS-10001",
  direction: "OUTBOUND",
  paymentMethod: "BANK_TRANSFER",
  amount: "2500.00",
  currency: "USD",
  businessReference: "BIZ-001",
  idempotencyKey: "IDEMP-001",
  sourceAccountReference: "VA-90001",
  beneficiaryReference: "BEN-31001",
  requestedChannelCode: "",
  narrative: "Vendor settlement",
  purposeCode: "SUPPLIER",
  crmCaseId: "CRM-CASE-001",
  salesOwner: "virtual.crm.owner",
  relationshipManager: "Alice Wong"
});

const paymentFilters = reactive({
  merchantId: "",
  status: "",
  direction: "",
  channelCode: "",
  keyword: ""
});

const paymentMethodOptions = ["VIRTUAL_ACCOUNT", "BANK_TRANSFER", "INTERNAL_TRANSFER", "POBO"];
const paymentStatusOptions = [
  "PENDING_CHECKER_REVIEW",
  "PENDING_L1_REVIEW",
  "PENDING_L2_REVIEW",
  "GATEWAY_SUBMITTED",
  "PROCESSING",
  "COMPLETED",
  "CANCELLED",
  "REJECTED",
  "FAILED"
];

const filteredCustomers = computed(() =>
  referenceData.customers.filter((item) => item.merchantId === form.merchantId)
);

const filteredBeneficiaries = computed(() =>
  referenceData.beneficiaries.filter((item) => item.merchantId === form.merchantId)
);

const filteredSourceAccounts = computed(() =>
  referenceData.sourceAccounts.filter((item) => item.merchantId === form.merchantId)
);

const routeOperation = computed(() => (form.direction === "OUTBOUND" ? "PAYOUT" : "VIRTUAL_ACCOUNT"));

const topRoute = computed(() =>
  routingRows.value.find((item) => item.rank === 1) ?? null
);

watch(
  () => form.direction,
  (direction) => {
    if (direction === "INBOUND") {
      form.paymentMethod = "VIRTUAL_ACCOUNT";
      form.beneficiaryReference = "";
    } else if (form.paymentMethod === "VIRTUAL_ACCOUNT") {
      form.paymentMethod = "BANK_TRANSFER";
    }
    loadRouting();
  }
);

watch(
  () => form.merchantId,
  () => {
    if (filteredCustomers.value[0]) form.customerReference = filteredCustomers.value[0].customerReference;
    if (filteredBeneficiaries.value[0] && form.direction === "OUTBOUND") {
      form.beneficiaryReference = filteredBeneficiaries.value[0].beneficiaryReference;
    }
    if (filteredSourceAccounts.value[0]) form.sourceAccountReference = filteredSourceAccounts.value[0].sourceAccountReference;
  }
);

onMounted(async () => {
  await Promise.all([loadReferenceData(), loadOrders(), loadChannels(), loadRouting()]);
});

async function requestJson(url, options = {}) {
  const response = await fetch(url, options);
  const body = await response.json();
  if (!response.ok || !body.success) {
    throw new Error(body.message || "Request failed");
  }
  return body.data;
}

async function loadReferenceData() {
  const data = await requestJson("/api/payment/reference-data");
  referenceData.merchants = data.merchants ?? [];
  referenceData.customers = data.customers ?? [];
  referenceData.beneficiaries = data.beneficiaries ?? [];
  referenceData.sourceAccounts = data.sourceAccounts ?? [];
}

async function loadOrders() {
  const params = new URLSearchParams();
  Object.entries(paymentFilters).forEach(([key, value]) => {
    if (value) params.set(key, value);
  });
  const suffix = params.toString() ? `?${params.toString()}` : "";
  const data = await requestJson(`/api/payment/orders${suffix}`);
  paymentOrders.value = data.orders ?? [];
}

async function loadChannels() {
  const data = await requestJson("/api/catalog/channels");
  channels.value = data.channels ?? [];
}

async function loadRouting() {
  const data = await requestJson(`/api/routing/recommendations?operation=${encodeURIComponent(routeOperation.value)}`);
  routingRows.value = data.recommendations ?? [];
}

async function createPaymentOrder() {
  paymentSubmitting.value = true;
  paymentError.value = "";
  try {
    const payload = {
      ...form,
      amount: Number(form.amount),
      beneficiaryReference: form.direction === "OUTBOUND" ? form.beneficiaryReference : null,
      requestedChannelCode: form.requestedChannelCode || null
    };
    paymentResponse.value = await requestJson("/api/payment/orders", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    await loadOrders();
  } catch (error) {
    paymentError.value = error.message;
  } finally {
    paymentSubmitting.value = false;
  }
}

async function actOnPayment(order, stage, decision) {
  paymentError.value = "";
  const actor = approvers[`${stage}Actor`];
  const comment = approvers[`${stage}Comment`];

  try {
    paymentResponse.value = await requestJson(`/api/payment/orders/${order.id}/${stage}/${decision}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ actor, comment })
    });
    await Promise.all([loadOrders(), loadRouting()]);
  } catch (error) {
    paymentError.value = error.message;
  }
}

async function runOpsAction(order, path) {
  paymentError.value = "";
  try {
    paymentResponse.value = await requestJson(`/api/payment/orders/${order.id}/${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        actor: approvers.opsActor,
        comment: approvers.opsComment
      })
    });
    await Promise.all([loadOrders(), loadRouting()]);
  } catch (error) {
    paymentError.value = error.message;
  }
}

function canApprove(order, stage) {
  return (
    (stage === "checker" && order.status === "PENDING_CHECKER_REVIEW") ||
    (stage === "l1" && order.status === "PENDING_L1_REVIEW") ||
    (stage === "l2" && order.status === "PENDING_L2_REVIEW")
  );
}

function canCancel(order) {
  return ["PENDING_CHECKER_REVIEW", "PENDING_L1_REVIEW", "PENDING_L2_REVIEW"].includes(order.status);
}

function canMarkCompleted(order) {
  return ["GATEWAY_SUBMITTED", "PROCESSING"].includes(order.status);
}

function canMarkFailed(order) {
  return ["GATEWAY_SUBMITTED", "PROCESSING"].includes(order.status);
}

function canRetry(order) {
  return order.status === "FAILED";
}
</script>

<template>
  <section class="content-stack">
    <section class="card content-card">
      <div class="section-header">
        <div>
          <p class="eyebrow">Payment Input</p>
          <h2>Inbound / Outbound order capture</h2>
          <p class="muted">
            当前支持订单创建、审批流、路由建议，以及网关提交后的运营动作。
          </p>
        </div>
        <button type="button" class="primary-button" :disabled="paymentSubmitting" @click="createPaymentOrder">
          {{ paymentSubmitting ? "Creating..." : "Create Payment Order" }}
        </button>
      </div>

      <div class="form-grid">
        <label>
          <span class="field-label">Merchant</span>
          <select v-model="form.merchantId" class="input">
            <option v-for="merchant in referenceData.merchants" :key="merchant.merchantId" :value="merchant.merchantId">
              {{ merchant.merchantId }} / {{ merchant.merchantName }}
            </option>
          </select>
        </label>
        <label>
          <span class="field-label">Customer</span>
          <select v-model="form.customerReference" class="input">
            <option v-for="customer in filteredCustomers" :key="customer.customerReference" :value="customer.customerReference">
              {{ customer.customerReference }} / {{ customer.customerName }}
            </option>
          </select>
        </label>
        <label><span class="field-label">Direction</span><select v-model="form.direction" class="input"><option value="INBOUND">INBOUND</option><option value="OUTBOUND">OUTBOUND</option></select></label>
        <label><span class="field-label">Payment Method</span><select v-model="form.paymentMethod" class="input"><option v-for="option in paymentMethodOptions" :key="option" :value="option">{{ option }}</option></select></label>
        <label><span class="field-label">Amount</span><input v-model="form.amount" type="number" min="0" step="0.01" class="input" /></label>
        <label><span class="field-label">Currency</span><input v-model="form.currency" class="input" /></label>
        <label><span class="field-label">Business Reference</span><input v-model="form.businessReference" class="input" /></label>
        <label><span class="field-label">Idempotency Key</span><input v-model="form.idempotencyKey" class="input" /></label>
        <label>
          <span class="field-label">Source Account</span>
          <select v-model="form.sourceAccountReference" class="input">
            <option value="">None</option>
            <option v-for="account in filteredSourceAccounts" :key="account.sourceAccountReference" :value="account.sourceAccountReference">
              {{ account.sourceAccountReference }} / {{ account.accountName }}
            </option>
          </select>
        </label>
        <label v-if="form.direction === 'OUTBOUND'">
          <span class="field-label">Beneficiary</span>
          <select v-model="form.beneficiaryReference" class="input">
            <option value="">Select beneficiary</option>
            <option v-for="beneficiary in filteredBeneficiaries" :key="beneficiary.beneficiaryReference" :value="beneficiary.beneficiaryReference">
              {{ beneficiary.beneficiaryReference }} / {{ beneficiary.beneficiaryName }}
            </option>
          </select>
        </label>
        <label>
          <span class="field-label">Requested Channel</span>
          <select v-model="form.requestedChannelCode" class="input">
            <option value="">Auto route by success rate</option>
            <option v-for="channel in channels" :key="channel.code" :value="channel.code">{{ channel.code }}</option>
          </select>
        </label>
        <label><span class="field-label">Purpose Code</span><input v-model="form.purposeCode" class="input" /></label>
        <label class="span-2"><span class="field-label">Narrative</span><input v-model="form.narrative" class="input" /></label>
        <label><span class="field-label">CRM Case ID</span><input v-model="form.crmCaseId" class="input" /></label>
        <label><span class="field-label">Sales Owner</span><input v-model="form.salesOwner" class="input" /></label>
        <label class="span-2"><span class="field-label">Relationship Manager</span><input v-model="form.relationshipManager" class="input" /></label>
      </div>

      <div class="info-strip">
        <span class="badge">Idempotent create</span>
        <span class="badge soft">Duplicate submit returns the same order</span>
        <span v-if="topRoute" class="badge soft">
          Suggested {{ routeOperation }} channel: {{ topRoute.channelCode }}
        </span>
      </div>

      <p v-if="paymentError" class="error-text">{{ paymentError }}</p>
    </section>

    <section class="card content-card">
      <div class="section-header">
        <div>
          <p class="eyebrow">Payment Search</p>
          <h2>Operations filters</h2>
        </div>
        <button type="button" class="secondary-button" @click="loadOrders">Apply Filters</button>
      </div>

      <div class="form-grid">
        <label><span class="field-label">Merchant</span><input v-model="paymentFilters.merchantId" class="input" /></label>
        <label><span class="field-label">Status</span><select v-model="paymentFilters.status" class="input"><option value="">ALL</option><option v-for="status in paymentStatusOptions" :key="status" :value="status">{{ status }}</option></select></label>
        <label><span class="field-label">Direction</span><select v-model="paymentFilters.direction" class="input"><option value="">ALL</option><option value="INBOUND">INBOUND</option><option value="OUTBOUND">OUTBOUND</option></select></label>
        <label><span class="field-label">Channel</span><input v-model="paymentFilters.channelCode" class="input" /></label>
        <label class="span-2"><span class="field-label">Keyword</span><input v-model="paymentFilters.keyword" class="input" placeholder="paymentNo / businessReference / idempotencyKey" /></label>
      </div>
    </section>

    <section class="card content-card">
      <div class="section-header">
        <div>
          <p class="eyebrow">Approval Actors</p>
          <h2>Checker / L1 / L2 / Ops setup</h2>
        </div>
      </div>

      <div class="form-grid">
        <label><span class="field-label">Checker Actor</span><input v-model="approvers.checkerActor" class="input" /></label>
        <label><span class="field-label">Checker Comment</span><input v-model="approvers.checkerComment" class="input" /></label>
        <label><span class="field-label">L1 Actor</span><input v-model="approvers.l1Actor" class="input" /></label>
        <label><span class="field-label">L1 Comment</span><input v-model="approvers.l1Comment" class="input" /></label>
        <label><span class="field-label">L2 Actor</span><input v-model="approvers.l2Actor" class="input" /></label>
        <label><span class="field-label">L2 Comment</span><input v-model="approvers.l2Comment" class="input" /></label>
        <label><span class="field-label">Ops Actor</span><input v-model="approvers.opsActor" class="input" /></label>
        <label><span class="field-label">Ops Comment</span><input v-model="approvers.opsComment" class="input" /></label>
      </div>
    </section>

    <section class="card content-card">
      <div class="section-header">
        <div>
          <p class="eyebrow">Payment Orders</p>
          <h2>State machine + approvals + ops actions</h2>
        </div>
        <button type="button" class="secondary-button" @click="loadOrders">Refresh Orders</button>
      </div>

      <div class="payment-list">
        <article v-for="order in paymentOrders" :key="order.id" class="payment-card">
          <div class="payment-card-head">
            <div>
              <h3>{{ order.paymentNo }}</h3>
              <p class="muted small">{{ order.direction }} / {{ order.paymentMethod }} / {{ order.amount }} {{ order.currency }}</p>
            </div>
            <span class="status-pill">{{ order.status }}</span>
          </div>

          <div class="detail-grid">
            <div><strong>Merchant:</strong> {{ order.merchantId }} / {{ order.merchantName }}</div>
            <div><strong>Customer:</strong> {{ order.customerReference }} / {{ order.customerName }}</div>
            <div><strong>Business Ref:</strong> {{ order.businessReference }}</div>
            <div><strong>Idempotency:</strong> {{ order.idempotencyKey }}</div>
            <div><strong>Requested Channel:</strong> {{ order.requestedChannelCode || "AUTO" }}</div>
            <div><strong>Routed Channel:</strong> {{ order.routedChannelCode || "-" }}</div>
            <div><strong>Gateway Operation:</strong> {{ order.gatewayOperation || "-" }}</div>
            <div><strong>Gateway Request ID:</strong> {{ order.gatewayRequestId || "-" }}</div>
            <div><strong>CRM Case:</strong> {{ order.crmCaseId }}</div>
            <div><strong>RM:</strong> {{ order.relationshipManager }}</div>
          </div>

          <div class="action-row">
            <button v-if="canApprove(order, 'checker')" type="button" class="secondary-button" @click="actOnPayment(order, 'checker', 'approve')">Checker Approve</button>
            <button v-if="canApprove(order, 'checker')" type="button" class="danger-button" @click="actOnPayment(order, 'checker', 'reject')">Checker Reject</button>
            <button v-if="canApprove(order, 'l1')" type="button" class="secondary-button" @click="actOnPayment(order, 'l1', 'approve')">L1 Approve</button>
            <button v-if="canApprove(order, 'l1')" type="button" class="danger-button" @click="actOnPayment(order, 'l1', 'reject')">L1 Reject</button>
            <button v-if="canApprove(order, 'l2')" type="button" class="primary-button" @click="actOnPayment(order, 'l2', 'approve')">L2 Approve + Submit</button>
            <button v-if="canApprove(order, 'l2')" type="button" class="danger-button" @click="actOnPayment(order, 'l2', 'reject')">L2 Reject</button>
            <button v-if="canCancel(order)" type="button" class="danger-button" @click="runOpsAction(order, 'cancel')">Cancel</button>
            <button v-if="canMarkCompleted(order)" type="button" class="primary-button" @click="runOpsAction(order, 'ops/complete')">Mark Completed</button>
            <button v-if="canMarkFailed(order)" type="button" class="danger-button" @click="runOpsAction(order, 'ops/fail')">Mark Failed</button>
            <button v-if="canRetry(order)" type="button" class="secondary-button" @click="runOpsAction(order, 'ops/retry')">Retry Submit</button>
          </div>

          <div class="approval-timeline">
            <div v-for="approval in order.approvals" :key="`${approval.stage}-${approval.actedAt}`" class="timeline-item">
              <strong>{{ approval.stage }} {{ approval.decision }}</strong>
              <span>{{ approval.actor }}</span>
              <span>{{ approval.comment }}</span>
              <span>{{ approval.actedAt }}</span>
            </div>
            <div v-if="order.approvals.length === 0" class="muted small">No approvals yet.</div>
          </div>

          <div class="approval-timeline">
            <div v-for="event in order.events || []" :key="`${event.eventType}-${event.eventAt}`" class="timeline-item">
              <strong>{{ event.eventType }}</strong>
              <span>{{ event.actor }}</span>
              <span>{{ event.comment }}</span>
              <span>{{ event.eventAt }}</span>
            </div>
            <div v-if="!order.events || order.events.length === 0" class="muted small">No payment events yet.</div>
          </div>
        </article>
      </div>
    </section>

    <section class="card inspector-panel">
      <p class="eyebrow">Last Payment Response</p>
      <pre>{{ paymentResponse ? JSON.stringify(paymentResponse, null, 2) : "No payment action yet..." }}</pre>
    </section>
  </section>
</template>
