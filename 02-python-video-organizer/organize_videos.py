# -*- coding: utf-8 -*-
"""视频素材整理助手：清洗、去重、生成建议文件名并输出统计。"""

import argparse
import csv
import re
import sys
from collections import Counter, OrderedDict
from pathlib import Path


REQUIRED_FIELDS = ["日期", "任务编号", "动作名称", "拍摄序号"]
OUTPUT_FIELDS = [
    "日期", "任务编号", "动作名称", "拍摄序号",
    "是否合格", "文件路径", "备注", "建议文件名",
]


def normalize_date(raw: str) -> str:
    """把 2026-07-15 / 2026/7/5 统一成 20260715。"""
    nums = re.findall(r"\d+", raw)
    if len(nums) < 3:
        return ""
    year, month, day = nums[0], nums[1].zfill(2), nums[2].zfill(2)
    if len(year) != 4 or not 1 <= int(month) <= 12 or not 1 <= int(day) <= 31:
        return ""
    return f"{year}{month}{day}"


def clean_row(row: dict) -> tuple:
    """清洗一行，返回 (是否有效, 清洗后的字典, 问题说明)。"""
    cleaned = {key: (row.get(key) or "").strip() for key in row}
    problem = ""

    for field in REQUIRED_FIELDS:
        if not cleaned.get(field):
            problem = f"缺少字段：{field}"
            return False, cleaned, problem

    date = normalize_date(cleaned["日期"])
    if not date:
        problem = "日期格式无法识别"
        return False, cleaned, problem
    cleaned["日期"] = date

    task = cleaned["任务编号"].upper()
    if not re.fullmatch(r"[A-Z0-9\-_]+", task):
        problem = "任务编号包含非法字符"
        return False, cleaned, problem
    cleaned["任务编号"] = task

    shot = cleaned["拍摄序号"]
    if not shot.isdigit() or not 1 <= int(shot) <= 999:
        problem = "拍摄序号应为 1-999 的数字"
        return False, cleaned, problem
    cleaned["拍摄序号"] = f"{int(shot):03d}"

    return True, cleaned, problem


def make_filename(row: dict) -> str:
    safe_action = re.sub(r'[\\/:*?"<>| ]', "_", row["动作名称"])
    return f"{row['日期']}_{row['任务编号']}_{safe_action}_{row['拍摄序号']}.mp4"


def main() -> None:
    parser = argparse.ArgumentParser(description="整理机器人动作视频素材记录")
    parser.add_argument("--input", required=True, help="输入 CSV 文件路径")
    parser.add_argument("--output", default="整理结果.csv", help="输出 CSV 文件路径")
    args = parser.parse_args()

    input_path = Path(args.input)
    if not input_path.exists():
        print(f"[错误] 找不到输入文件：{input_path}", file=sys.stderr)
        return 1

    valid_rows = []
    skipped = []
    seen = set()
    duplicate_count = 0

    with input_path.open("r", encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        for line_no, raw_row in enumerate(reader, start=2):
            if raw_row is None:
                continue
            ok, cleaned, problem = clean_row(raw_row)
            if not ok:
                skipped.append((line_no, problem))
                continue

            cleaned["建议文件名"] = make_filename(cleaned)
            key = (cleaned["日期"], cleaned["任务编号"], cleaned["动作名称"], cleaned["拍摄序号"])
            if key in seen:
                duplicate_count += 1
                skipped.append((line_no, "重复记录"))
                continue
            seen.add(key)
            valid_rows.append(cleaned)

    output = [OrderedDict((k, row.get(k, "")) for k in OUTPUT_FIELDS) for row in valid_rows]
    with Path(args.output).open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=OUTPUT_FIELDS)
        writer.writeheader()
        writer.writerows(output)

    print(f"输入文件：{input_path.name}")
    print(f"有效记录：{len(valid_rows)} 条（已去重）")
    print(f"跳过记录：{len(skipped)} 条（其中重复 {duplicate_count} 条）")
    print(f"结果已保存：{args.output}")

    action_counter = Counter((r["任务编号"], r["动作名称"]) for r in valid_rows)
    if action_counter:
        print("\n按任务-动作统计：")
        for (task, action), count in action_counter.most_common():
            print(f"  {task} / {action}：{count} 条")


if __name__ == "__main__":
    raise SystemExit(main())
