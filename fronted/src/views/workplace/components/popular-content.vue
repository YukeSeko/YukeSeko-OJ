<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="{ padding: '17px 20px 21px 20px' }"
    >
      <template #title> 最近提交</template>
      <template #extra>
        <router-link to="/question_submit">
          <a-link>查看更多</a-link>
        </router-link>
      </template>
      <a-space direction="vertical" :size="10" fill>
        <a-table
          :data="renderList"
          :pagination="false"
          :bordered="false"
          :scroll="{ x: '100%', y: '264px' }"
        >
          <template #columns>
            <a-table-column
              title="题目名称"
              data-index="questionVO.title"
            ></a-table-column>
            <a-table-column title="标签" data-index="language">
              <template #cell="{ record }">
                <a-typography-paragraph
                  :ellipsis="{
                    rows: 1,
                  }"
                >
                  <a-tag color="green" bordered>{{ record.language }}</a-tag>
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
          </template>
        </a-table>
      </a-space>
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
import { onMounted, ref } from "vue";
import useLoading from "@/hooks/loading";
import type { TableData } from "@arco-design/web-vue/es/table/interface";
import moment from "moment";
import { QuestionControllerService } from "@/api/services/QuestionControllerService";
import { QuestionSubmitQueryRequest } from "@/api/models/QuestionSubmitQueryRequest";
import store from "@/store";

const type = ref("text");
const { loading, setLoading } = useLoading();
const renderList = ref<TableData[]>();
const searchParams = ref<QuestionSubmitQueryRequest>({
  questionId: undefined,
  language: undefined,
  pageSize: 6,
  current: 1,
});
const fetchData = async () => {
  try {
    setLoading(true);
    const res =
      await QuestionControllerService.listQuestionSubmitByPageUsingPost({
        ...searchParams.value,
        userId: store.state.user?.loginUser?.id,
        sortField: "createTime",
        sortOrder: "descend",
      });
    renderList.value = res.data.records;
  } catch (err) {
    // you can report use errorHandler or other
  } finally {
    setLoading(false);
  }
};
onMounted(() => {
  fetchData();
  console.log(store.state.user.loginUser);
});
// const typeChange = (contentType: string) => {
//   fetchData(contentType);
// };
// fetchData("text");
</script>

<style scoped lang="less">
.general-card {
  min-height: 395px;
}

:deep(.arco-table-tr) {
  height: 44px;

  .arco-typography {
    margin-bottom: 0;
  }
}

.increases-cell {
  display: flex;
  align-items: center;

  span {
    margin-right: 4px;
  }
}
</style>
