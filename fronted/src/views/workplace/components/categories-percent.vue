<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="{
        padding: '20px',
      }"
    >
      <template #title>做题分析</template>
      <ChartComponents height="310px" :option="chartOption" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
import useLoading from "@/hooks/loading";
import useChartOption from "@/hooks/chart-option";
import ChartComponents from "@/components/ChartComponents.vue";
import { onMounted, ref } from "vue";
import { QuestionControllerService } from "@/api/services/QuestionControllerService";

const { loading } = useLoading();
const questionUnSolveCount = ref(0);
const questionSolveCount = ref(0);
const questionCount = ref(0);
onMounted(async () => {
  try {
    //todo 修改请求接口，重新编写请求方法
    const personalData = await QuestionControllerService.getPersonalData();
    questionCount.value = parseInt(personalData.data.questionCount);
    questionSolveCount.value = parseInt(personalData.data.questionSolveCount);
    questionUnSolveCount.value = questionCount.value - questionSolveCount.value;
  } catch (e) {
    //
  }
});
const { chartOption } = useChartOption((isDark) => {
  // echarts support https://echarts.apache.org/zh/theme-builder.html
  // It's not used here
  return {
    legend: {
      left: "center",
      data: ["已通过题目", "未开始题目"],
      bottom: 0,
      icon: "circle",
      itemWidth: 8,
      textStyle: {
        color: isDark ? "rgba(255, 255, 255, 0.7)" : "#4E5969",
      },
      itemStyle: {
        borderWidth: 0,
      },
    },
    tooltip: {
      show: true,
      trigger: "item",
    },
    graphic: {
      elements: [
        {
          type: "text",
          left: "center",
          top: "40%",
          style: {
            text: "总题量",
            textAlign: "center",
            fill: isDark ? "#ffffffb3" : "#4E5969",
            fontSize: 14,
          },
        },
        {
          type: "text",
          left: "center",
          top: "50%",
          style: {
            text: questionCount.value,
            textAlign: "center",
            fill: isDark ? "#ffffffb3" : "#1D2129",
            fontSize: 16,
            fontWeight: 500,
          },
        },
      ],
    },
    series: [
      {
        type: "pie",
        radius: ["50%", "70%"],
        center: ["50%", "50%"],
        label: {
          formatter: "{d}%",
          fontSize: 14,
          color: isDark ? "rgba(255, 255, 255, 0.7)" : "#4E5969",
        },
        itemStyle: {
          borderColor: isDark ? "#232324" : "#fff",
          borderWidth: 1,
        },
        data: [
          {
            value: [questionSolveCount.value],
            name: "已通过题目",
            itemStyle: {
              color: isDark ? "#3D72F6" : "#249EFF",
            },
          },
          {
            value: [questionUnSolveCount.value],
            name: "提交未通过题目",
            itemStyle: {
              color: isDark ? "#A079DC" : "#313CA9",
            },
          },
          // {
          //   value: [445694],
          //   name: "未开始题目",
          //   itemStyle: {
          //     color: isDark ? "#6CAAF5" : "#21CCFF",
          //   },
          // },
        ],
      },
    ],
  };
});
</script>

<style scoped lang="less"></style>
