<template>
  <div id="viewQuestionView">
    <a-row :gutter="[24, 24]">
      <a-col :md="12" :xs="24">
        <a-tabs default-active-key="question" @change="tabChanges">
          <a-tab-pane key="question" title="题目">
            <a-card v-if="question" :title="question.title">
              <a-descriptions
                title="判题条件"
                :column="{ xs: 1, md: 2, lg: 3 }"
              >
                <a-descriptions-item label="时间限制">
                  {{ question.judgeConfig.timeLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="内存限制">
                  {{ question.judgeConfig.memoryLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="堆栈限制">
                  {{ question.judgeConfig.stackLimit ?? 0 }}
                </a-descriptions-item>
              </a-descriptions>
              <MdViewer :value="question.content || ''" />
              <template #extra>
                <a-space wrap>
                  <a-tag
                    v-for="(tag, index) of question.tags"
                    :key="index"
                    color="green"
                    >{{ tag }}
                  </a-tag>
                </a-space>
              </template>
            </a-card>
          </a-tab-pane>
          <!--          <a-tab-pane key="comment" title="评论" disabled> 评论区</a-tab-pane>-->
          <a-tab-pane key="answer" title="答案">
            <CodeViewer :value="answer" />
          </a-tab-pane>
          <a-tab-pane key="record" title="提交记录">
            <a-table :data="record" :pagination="false" :bordered="false">
              <template #columns>
                <a-table-column title="标签" data-index="language">
                  <template #cell="{ record }">
                    <a-typography-paragraph
                      :ellipsis="{
                        rows: 1,
                      }"
                    >
                      <a-tag style="margin-top: 10px" color="green" bordered
                        >{{ record.language }}
                      </a-tag>
                    </a-typography-paragraph>
                  </template>
                </a-table-column>
                <a-table-column title="判题结果">
                  <template #cell="{ record }">
                    <a-tag :color="record.status === 2 ? '#00b42a' : '#f53f3f'">
                      {{ record.judgeInfo }}
                    </a-tag>
                  </template>
                </a-table-column>
                <a-table-column
                  title="提交日期"
                  data-index="createTime"
                  :sortable="{
                    sortDirections: ['ascend', 'descend'],
                  }"
                >
                  <template #cell="{ record }">
                    <div class="increases-cell">
                      <span>{{
                        moment(record.createTime).format("YYYY-MM-DD")
                      }}</span>
                    </div>
                  </template>
                </a-table-column>
                <a-table-column title="操作">
                  <template #cell="{ record }">
                    <a-button @click="showDetails(record)">查看详情</a-button>
                  </template>
                </a-table-column>
              </template>
            </a-table>
            <!--            <a-list :max-height="500" :scrollbar="false">-->
            <!--              <a-list-item v-for="idx in record" :key="idx">-->
            <!--                <span-->
            <!--                  >提交时间：{{-->
            <!--                    moment(idx.updateTime).format("YYYY-MM-DD")-->
            <!--                  }}</span-->
            <!--                >-->
            <!--                <a style="margin-left: 20px">判题结果：{{ idx.judgeInfo }}</a>-->
            <!--                <template #actions>-->
            <!--                  <icon-edit />-->
            <!--                  <icon-delete />-->
            <!--                </template>-->
            <!--              </a-list-item>-->
            <!--            </a-list>-->
          </a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col :md="12" :xs="24" v-if="isShowDetails">
        <a-form :model="form" layout="inline">
          <a-form-item
            field="language"
            label="编程语言"
            style="min-width: 240px"
          >
            <a-select
              v-model="form.language"
              :style="{ width: '320px' }"
              placeholder="选择编程语言"
            >
              <a-option>java</a-option>
              <!--              <a-option>cpp</a-option>-->
              <!--              <a-option>go</a-option>-->
              <!--              <a-option>html</a-option>-->
            </a-select>
          </a-form-item>
        </a-form>
        <CodeEditor
          :value="form.code"
          :language="form.language"
          :handle-change="changeCode"
          code-default="aaaa"
        />
        <a-divider size="0" />
        <a-button type="primary" style="min-width: 200px" @click="doSubmit">
          提交代码
        </a-button>
      </a-col>
      <a-col :md="12" :xs="24" v-if="!isShowDetails">
        <RecordDetail
          :close-record="closeRecord"
          :record-content="recordDataCode"
        />
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, withDefaults, defineProps } from "vue";
import message from "@arco-design/web-vue/es/message";
import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import { QuestionVO } from "@/api/models/QuestionVO";
import { QuestionControllerService } from "@/api/services/QuestionControllerService";
import { QuestionSubmitAddRequest } from "@/api/models/QuestionSubmitAddRequest";
import moment from "moment";
import { TableData } from "@arco-design/web-vue/es/table/interface";
import store from "@/store";
import CodeViewer from "@/components/CodeViewer.vue";
import RecordDetail from "@/components/RecordDetail.vue";
import { QuestionSubmitVO } from "@/api/models/QuestionSubmitVO";

const question = ref<QuestionVO>();
const record = ref<TableData[]>();
const answer = ref<string>();
const isShowDetails = ref<boolean>(true);
const recordDataCode = ref<QuestionSubmitVO>();

interface Props {
  id: string;
}

const closeRecord = () => {
  isShowDetails.value = !isShowDetails.value;
};

/**
 * 当tabs标签变化时
 * @param key
 */
const tabChanges = async (key: string) => {
  if (key === "record") {
    const res =
      await QuestionControllerService.listQuestionSubmitByPageUsingPost({
        userId: store.state.user?.loginUser?.id,
        questionId: question.value?.id,
        sortField: "createTime",
        sortOrder: "descend",
      });
    if (res.code === 0) {
      record.value = res.data.records;
    } else {
      message.error("加载失败，" + res.message);
    }
  } else if (key === "answer") {
    //请求答案
    const res = await QuestionControllerService.getQuestionAnswer(
      question.value?.id as any
    );
    answer.value = res.data;
  }
};

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const loadData = async () => {
  const res = await QuestionControllerService.getQuestionVoByIdUsingGet(
    props.id as any
  );
  if (res.code === 0) {
    question.value = res.data;
  } else {
    message.error("加载失败，" + res.message);
  }
};

const form = ref<QuestionSubmitAddRequest>({
  language: "java",
  code:
    "import java.io.*;\n" +
    "import java.util.*;\n" +
    "public class Main{\n" +
    "    public static void main(String[] args){\n" +
    "        Scanner sc = new Scanner(System.in);\n" +
    "        // 请输入你的代码 \n" +
    "    }\n" +
    "}",
});

const showDetails = (record: any) => {
  recordDataCode.value = record;
  isShowDetails.value = false;
};
/**
 * 提交代码
 */
const doSubmit = async () => {
  if (!question.value?.id) {
    return;
  }

  const res = await QuestionControllerService.doQuestionSubmitUsingPost({
    ...form.value,
    questionId: question.value.id,
  });
  if (res.code === 0) {
    message.success("提交成功");
  } else {
    message.error("提交失败," + res.message);
  }
};

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

const changeCode = (value: string) => {
  form.value.code = value;
};
</script>

<style>
#viewQuestionView {
  max-width: 1400px;
  margin: 0 auto;
}

#viewQuestionView .arco-space-horizontal .arco-space-item {
  margin-bottom: 0 !important;
}
</style>
