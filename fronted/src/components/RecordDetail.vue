<template>
  <div id="RecordDetail">
    <a-card class="recordDetail">
      <div class="header">
        <icon-close class="closeIcon" @click="props.closeRecord" />
      </div>
      <div class="main">
        <a-scrollbar style="height: calc(100vh - 120px); overflow: auto">
          <!--          <div class="info">-->
          <!--            <a-row>-->
          <!--              <a-col :span="6" :offset="6">-->
          <!--                <div class="recordInfo">-->
          <!--                  <div-->
          <!--                    class="suc center"-->
          <!--                    v-if="props.recordContent.judgeInfo?.result === '成功'"-->
          <!--                  >-->
          <!--                    <icon-check-circle :style="{ fontSize: '24px' }" />-->
          <!--                    <span class="recordInfoText"> 成功 </span>-->
          <!--                  </div>-->
          <!--                  <div class="err center" v-else>-->
          <!--                    <icon-close-circle :style="{ fontSize: '24px' }" />-->
          <!--                    <span class="recordInfoText">-->
          <!--                      {{ props.recordContent.judgeInfo }}-->
          <!--                    </span>-->
          <!--                  </div>-->
          <!--                </div>-->
          <!--              </a-col>-->
          <!--            </a-row>-->
          <!--          </div>-->
          <div class="message">
            <p class="label" style="font-size: 20px">判题信息</p>
            <a-alert
              style="margin-top: 20px"
              :type="props.recordContent.status === 2 ? 'success' : 'error'"
              >{{ props.recordContent.judgeInfo }}
            </a-alert>
          </div>
          <div class="tag">
            <p class="label">
              语言：
              <a-tag color="arcoblue" bordered size="small">
                {{ props.recordContent.language }}
              </a-tag>
            </p>
            <p class="label" v-if="props.recordContent.status === 2">
              运行内存：
              <a-tag color="arcoblue" bordered size="small">
                {{ props.recordContent.detailsInfo.memory }}MB
              </a-tag>
            </p>
            <p class="label" v-if="props.recordContent.status === 2">
              执行时间：
              <a-tag color="arcoblue" bordered size="small">
                {{ props.recordContent.detailsInfo.time }}ms
              </a-tag>
            </p>
          </div>
          <div class="code">
            <p class="label">输入代码：</p>
            <MdViewer :value="codeContent" />
          </div>
        </a-scrollbar>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import MdViewer from "@/components/MdViewer.vue";
import {
  ref,
  watchEffect,
  withDefaults,
  defineProps,
  onMounted,
  watch,
} from "vue";
import { useStore } from "vuex";
import { QuestionSubmitVO } from "@/api/models/QuestionSubmitVO";

interface Props {
  recordContent: QuestionSubmitVO;
  closeRecord: () => void;
}

const props = withDefaults(defineProps<Props>(), {});
const codeContent = ref();
const v1 = "```";

watchEffect(() => {
  console.log(props.recordContent);
  codeContent.value = `${v1}${props.recordContent.language}\n${props.recordContent.code}`;
  console.log(codeContent.value);
});

const store = useStore();
// 获取相关信息
</script>

<style scoped>
:deep(.arco-card-body) {
  padding: 8px 16px;
}

#RecordDetail {
  height: 100%;
}

.recordDetail {
  height: 100%;
}

.header {
  padding-bottom: 5px;
  border-bottom: 1px solid #eee;
}

.closeIcon {
  color: gray;
  cursor: pointer;
}

.info {
  margin: 16px 0;
}

.useInfo {
  display: flex;
}

.avatar {
  margin-top: 6px;
  margin-right: 4px;
}

.avatar-image {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  object-fit: cover;
}

:deep(.arco-avatar) {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  object-fit: cover;
}

p {
  margin: 4px;
}

.userName {
  font-weight: 500;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
}

.recordTime {
  color: rgb(var(--gray-10));
  font-size: 10px;
}

.recordInfo {
  margin-top: 8px;
}

.err {
  color: #ea2027;
}

.suc {
  color: green;
}

.center {
  display: flex;
  justify-content: center;
  align-content: center;
}

.recordInfoText {
  margin-left: 6px;
  font-size: 20px;
  font-weight: 600;
}

.label {
  margin: 16px 0 0 4px;
  font-size: 12px;
  color: rgb(var(--gray-10));
}

.messageBox {
  background: #000a200d;
  padding: 12px;
  margin-top: 10px;
}

.message {
  margin: 20px 0;
}
</style>
