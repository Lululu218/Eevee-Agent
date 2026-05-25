<template>
  <div class="code-highlighter">
    <div v-if="language" class="code-header">
      <span class="language">{{ language }}</span>
      <button class="copy-btn" @click="copyCode">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect width="14" height="14" x="8" y="8" rx="2" ry="2"></rect>
          <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"></path>
        </svg>
        {{ copied ? '已复制' : '复制' }}
      </button>
    </div>
    <pre class="code-container"><code ref="codeRef" class="hljs" :class="`language-${language}`" v-html="highlightedCode"></code></pre>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  language: {
    type: String,
    default: 'plaintext'
  }
});

const codeRef = ref(null);
const copied = ref(false);
const highlightedCode = ref('');
let highlightTimer = null;

// 简单的代码高亮实现（支持常见语言）
const highlightSyntax = (code, lang) => {
  if (!code) return '';
  
  let result = code
    // HTML转义
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
  
  // 根据语言应用不同的高亮规则
  switch (lang.toLowerCase()) {
    case 'javascript':
    case 'js':
      result = highlightJavaScript(result);
      break;
    case 'python':
    case 'py':
      result = highlightPython(result);
      break;
    case 'java':
      result = highlightJava(result);
      break;
    case 'json':
      result = highlightJson(result);
      break;
    case 'html':
    case 'xml':
      result = highlightHtml(result);
      break;
    case 'css':
      result = highlightCss(result);
      break;
    case 'sql':
      result = highlightSql(result);
      break;
    default:
      // 默认：关键字高亮
      result = highlightDefault(result);
  }
  
  return result;
};

// JavaScript高亮
const highlightJavaScript = (code) => {
  const keywords = ['const', 'let', 'var', 'function', 'return', 'if', 'else', 'for', 'while', 'class', 'import', 'export', 'from', 'async', 'await', 'new', 'this', 'try', 'catch', 'throw', 'typeof', 'instanceof', 'null', 'undefined', 'true', 'false'];
  
  let result = code;
  
  // 字符串
  result = result.replace(/(['"`])((?:\\.|[^\\])*?)\1/g, '<span class="hljs-string">$1$2$1</span>');
  
  // 数字
  result = result.replace(/\b(\d+\.?\d*)\b/g, '<span class="hljs-number">$1</span>');
  
  // 关键字
  keywords.forEach(keyword => {
    const regex = new RegExp(`\\b(${keyword})\\b`, 'g');
    result = result.replace(regex, '<span class="hljs-keyword">$1</span>');
  });
  
  // 注释
  result = result.replace(/(\/\/.*$)/gm, '<span class="hljs-comment">$1</span>');
  result = result.replace(/(\/\*[\s\S]*?\*\/)/g, '<span class="hljs-comment">$1</span>');
  
  // 函数名
  result = result.replace(/\b([a-zA-Z_][a-zA-Z0-9_]*)\s*\(/g, '<span class="hljs-function">$1</span>(');
  
  return result;
};

// Python高亮
const highlightPython = (code) => {
  const keywords = ['def', 'class', 'return', 'if', 'elif', 'else', 'for', 'while', 'import', 'from', 'as', 'try', 'except', 'finally', 'raise', 'yield', 'lambda', 'and', 'or', 'not', 'is', 'in', 'True', 'False', 'None'];
  
  let result = code;
  
  // 字符串
  result = result.replace(/(['"])((?:\\.|[^\\])*?)\1/g, '<span class="hljs-string">$1$2$1</span>');
  result = result.replace(/('''[\s\S]*?''')|("""[\s\S]*?""")/g, '<span class="hljs-string">$1$2</span>');
  
  // 数字
  result = result.replace(/\b(\d+\.?\d*)\b/g, '<span class="hljs-number">$1</span>');
  
  // 关键字
  keywords.forEach(keyword => {
    const regex = new RegExp(`\\b(${keyword})\\b`, 'g');
    result = result.replace(regex, '<span class="hljs-keyword">$1</span>');
  });
  
  // 注释
  result = result.replace(/(#.*$)/gm, '<span class="hljs-comment">$1</span>');
  
  // 函数名
  result = result.replace(/\bdef\s+([a-zA-Z_][a-zA-Z0-9_]*)/g, 'def <span class="hljs-function">$1</span>');
  
  return result;
};

// Java高亮
const highlightJava = (code) => {
  const keywords = ['public', 'private', 'protected', 'static', 'final', 'void', 'int', 'String', 'boolean', 'if', 'else', 'for', 'while', 'class', 'interface', 'extends', 'implements', 'return', 'throw', 'try', 'catch', 'finally', 'new', 'this', 'super', 'true', 'false', 'null', 'package', 'import'];
  
  let result = code;
  
  // 字符串
  result = result.replace(/(['"])((?:\\.|[^\\])*?)\1/g, '<span class="hljs-string">$1$2$1</span>');
  
  // 数字
  result = result.replace(/\b(\d+\.?\d*)\b/g, '<span class="hljs-number">$1</span>');
  
  // 关键字
  keywords.forEach(keyword => {
    const regex = new RegExp(`\\b(${keyword})\\b`, 'g');
    result = result.replace(regex, '<span class="hljs-keyword">$1</span>');
  });
  
  // 注释
  result = result.replace(/(\/\/.*$)/gm, '<span class="hljs-comment">$1</span>');
  result = result.replace(/(\/\*[\s\S]*?\*\/)/g, '<span class="hljs-comment">$1</span>');
  
  // 类名和方法名
  result = result.replace(/\b([A-Z][a-zA-Z0-9_]*)\s*\(/g, '<span class="hljs-function">$1</span>(');
  result = result.replace(/\bclass\s+([A-Z][a-zA-Z0-9_]*)/g, 'class <span class="hljs-title">$1</span>');
  
  return result;
};

// JSON高亮
const highlightJson = (code) => {
  let result = code;
  
  // 键名
  result = result.replace(/"([^"]+)":/g, '<span class="hljs-attr">"$1"</span>:');
  
  // 字符串值
  result = result.replace(/: "((?:\\.|[^\\])*?)"/g, ': <span class="hljs-string">"$1"</span>');
  
  // 数字
  result = result.replace(/:\s*(-?\d+\.?\d*)/g, ': <span class="hljs-number">$1</span>');
  
  // 布尔值和null
  result = result.replace(/\b(true|false|null)\b/g, '<span class="hljs-keyword">$1</span>');
  
  return result;
};

// HTML高亮
const highlightHtml = (code) => {
  let result = code;
  
  // 标签
  result = result.replace(/(<\/?[a-zA-Z][a-zA-Z0-9]*(\s[^>]*)?>)/g, '<span class="hljs-tag">$1</span>');
  
  // 属性
  result = result.replace(/(\s+[a-zA-Z_][a-zA-Z0-9_]*)\s*=/g, '<span class="hljs-attr">$1</span>=');
  
  // 属性值
  result = result.replace(/=("(?:\\.|[^\\])*?"|'(?:\\.|[^\\])*?')/g, '=<span class="hljs-string">$1</span>');
  
  // 注释
  result = result.replace(/(<!--[\s\S]*?-->)/g, '<span class="hljs-comment">$1</span>');
  
  return result;
};

// CSS高亮
const highlightCss = (code) => {
  const keywords = ['color', 'background', 'font', 'margin', 'padding', 'border', 'width', 'height', 'display', 'position', 'float', 'clear', 'top', 'left', 'right', 'bottom', 'z-index', 'overflow', 'opacity', 'transition', 'transform'];
  
  let result = code;
  
  // 属性名
  result = result.replace(/([a-z-]+)\s*:/g, '<span class="hljs-property">$1</span>:');
  
  // 属性值 - 颜色
  result = result.replace(/:\s*(#[0-9a-fA-F]+|rgb\([^)]+\)|rgba\([^)]+\))/g, ': <span class="hljs-string">$1</span>');
  
  // 属性值 - 长度
  result = result.replace(/:\s*(\d+px|\d+em|\d+rem|\d+%)/g, ': <span class="hljs-number">$1</span>');
  
  // 属性值 - 关键字
  keywords.forEach(keyword => {
    const regex = new RegExp(`:\\s*(${keyword})\\b`, 'g');
    result = result.replace(regex, ': <span class="hljs-keyword">$1</span>');
  });
  
  // 注释
  result = result.replace(/(\/\*[\s\S]*?\*\/)/g, '<span class="hljs-comment">$1</span>');
  
  // 选择器
  result = result.replace(/^([^{]+){/gm, '<span class="hljs-selector">$1</span>{');
  
  return result;
};

// SQL高亮
const highlightSql = (code) => {
  const keywords = ['SELECT', 'FROM', 'WHERE', 'AND', 'OR', 'NOT', 'IN', 'LIKE', 'BETWEEN', 'JOIN', 'LEFT', 'RIGHT', 'INNER', 'OUTER', 'ON', 'GROUP', 'BY', 'HAVING', 'ORDER', 'LIMIT', 'INSERT', 'INTO', 'VALUES', 'UPDATE', 'SET', 'DELETE', 'CREATE', 'TABLE', 'DROP', 'ALTER', 'ADD', 'INDEX', 'PRIMARY', 'KEY', 'FOREIGN', 'REFERENCES', 'NULL', 'NOT', 'DEFAULT', 'UNIQUE', 'CHECK', 'CONSTRAINT'];
  
  let result = code;
  
  // 字符串
  result = result.replace(/('(?:\\.|[^\\])*?')/g, '<span class="hljs-string">$1</span>');
  
  // 数字
  result = result.replace(/\b(\d+\.?\d*)\b/g, '<span class="hljs-number">$1</span>');
  
  // 关键字（不区分大小写）
  keywords.forEach(keyword => {
    const regex = new RegExp(`\\b(${keyword})\\b`, 'gi');
    result = result.replace(regex, '<span class="hljs-keyword">$1</span>');
  });
  
  // 注释
  result = result.replace(/(--.*$)/gm, '<span class="hljs-comment">$1</span>');
  result = result.replace(/(\/\*[\s\S]*?\*\/)/g, '<span class="hljs-comment">$1</span>');
  
  // 表名和列名
  result = result.replace(/\b([a-zA-Z_][a-zA-Z0-9_]*)\./g, '<span class="hljs-title">$1</span>.');
  
  return result;
};

// 默认高亮
const highlightDefault = (code) => {
  // 字符串
  code = code.replace(/(['"`])((?:\\.|[^\\])*?)\1/g, '<span class="hljs-string">$1$2$1</span>');
  
  // 数字
  code = code.replace(/\b(\d+\.?\d*)\b/g, '<span class="hljs-number">$1</span>');
  
  // 注释
  code = code.replace(/(\/\/.*$)/gm, '<span class="hljs-comment">$1</span>');
  code = code.replace(/(\/\*[\s\S]*?\*\/)/g, '<span class="hljs-comment">$1</span>');
  
  return code;
};

// 延迟高亮，避免频繁更新
const scheduleHighlight = () => {
  if (highlightTimer) {
    clearTimeout(highlightTimer);
  }
  highlightTimer = setTimeout(() => {
    highlightedCode.value = highlightSyntax(props.content, props.language);
  }, 50);
};

// 监听内容变化
watch(() => props.content, () => {
  scheduleHighlight();
});

onMounted(() => {
  scheduleHighlight();
});

onUnmounted(() => {
  if (highlightTimer) {
    clearTimeout(highlightTimer);
  }
});

// 复制代码
const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(props.content);
    copied.value = true;
    setTimeout(() => {
      copied.value = false;
    }, 2000);
  } catch (error) {
    console.error('复制失败:', error);
  }
};
</script>

<style scoped>
.code-highlighter {
  background: #1e1e1e;
  border-radius: 8px;
  overflow: hidden;
  margin: 8px 0;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #2d2d2d;
  border-bottom: 1px solid #3d3d3d;
}

.language {
  font-size: 12px;
  color: #858585;
  font-weight: 500;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  font-size: 12px;
  color: #858585;
  background: transparent;
  border: 1px solid #3d3d3d;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.copy-btn:hover {
  background: #3d3d3d;
  color: #fff;
}

.code-container {
  margin: 0;
  padding: 12px;
  overflow-x: auto;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #d4d4d4;
}

.code-container::-webkit-scrollbar {
  height: 8px;
}

.code-container::-webkit-scrollbar-track {
  background: #2d2d2d;
}

.code-container::-webkit-scrollbar-thumb {
  background: #4d4d4d;
  border-radius: 4px;
}

.code-container::-webkit-scrollbar-thumb:hover {
  background: #5d5d5d;
}

/* 高亮样式 */
.hljs-keyword {
  color: #569cd6;
}

.hljs-string {
  color: #ce9178;
}

.hljs-number {
  color: #b5cea8;
}

.hljs-comment {
  color: #6a9955;
  font-style: italic;
}

.hljs-function {
  color: #dcdcaa;
}

.hljs-title {
  color: #4ec9b0;
}

.hljs-attr {
  color: #9cdcfe;
}

.hljs-tag {
  color: #569cd6;
}

.hljs-property {
  color: #9cdcfe;
}

.hljs-selector {
  color: #d7ba7d;
}
</style>
