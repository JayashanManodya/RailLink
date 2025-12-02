(() => {
  const widget = document.getElementById('rail-chat-widget');
  if (!widget) {
    return;
  }

  const toggleBtn = widget.querySelector('[data-chat-toggle]');
  const panel = widget.querySelector('[data-chat-panel]');
  const closeBtn = widget.querySelector('[data-chat-close]');
  const messageList = widget.querySelector('[data-chat-messages]');
  const form = widget.querySelector('[data-chat-form]');
  const input = widget.querySelector('[data-chat-input]');
  const status = widget.querySelector('[data-chat-status]');
  const suggestionsContainer = widget.querySelector('[data-chat-suggestions]');
  const errorBanner = widget.querySelector('[data-chat-error]');

  if (!toggleBtn || !panel || !form || !input || !messageList) {
    return;
  }

  const SESSION_KEY = 'raillink.chat.sessionId';
  const historyLimit = 12;
  const state = {
    sessionId: window.localStorage.getItem(SESSION_KEY) || generateSessionId(),
    busy: false,
    initialized: false,
    history: []
  };

  window.localStorage.setItem(SESSION_KEY, state.sessionId);

  toggleBtn.addEventListener('click', () => {
    const expanded = panel.classList.toggle('hidden');
    toggleBtn.setAttribute('aria-expanded', (!expanded).toString());
    if (!panel.classList.contains('hidden') && !state.initialized) {
      bootstrapAssistant();
      state.initialized = true;
    }
    if (!panel.classList.contains('hidden')) {
      input.focus();
    }
  });

  if (closeBtn) {
    closeBtn.addEventListener('click', () => {
      panel.classList.add('hidden');
      toggleBtn.setAttribute('aria-expanded', 'false');
    });
  }

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    if (state.busy) {
      return;
    }

    const text = input.value.trim();
    if (!text) {
      return;
    }

    pushMessage('user', text);
    input.value = '';
    dispatchMessage(text);
  });

  suggestionsContainer?.addEventListener('click', (event) => {
    const button = event.target.closest('button[data-suggestion]');
    if (!button || state.busy) {
      return;
    }
    const payload = button.getAttribute('data-suggestion');
    if (payload) {
      pushMessage('user', payload);
      dispatchMessage(payload);
    }
  });

  function bootstrapAssistant() {
    pushMessage('assistant', 'Hi there! I am the RailLink assistant. Ask me about bookings, schedules or anything else.');
  }

  function pushMessage(role, content) {
    const entry = { role, content, timestamp: new Date().toISOString() };
    state.history.push(entry);
    if (state.history.length > historyLimit) {
      state.history.splice(0, state.history.length - historyLimit);
    }

    const bubble = document.createElement('div');
    bubble.className = role === 'user' ? 'chat-bubble chat-bubble-user' : 'chat-bubble chat-bubble-agent';
    bubble.innerHTML = `<p>${escapeHtml(content)}</p>`;
    messageList.appendChild(bubble);
    messageList.scrollTop = messageList.scrollHeight;
  }

  function showError(message) {
    if (!errorBanner) {
      return;
    }
    errorBanner.textContent = message || '';
    errorBanner.classList.toggle('hidden', !message);
  }

  async function dispatchMessage(text) {
    setBusy(true);
    showError('');
    renderSuggestions([]);

    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sessionId: state.sessionId,
          message: text,
          history: state.history,
          metadata: {
            pathname: window.location.pathname,
            userAgent: navigator.userAgent,
            locale: navigator.language
          }
        })
      });

      if (response.status === 401) {
        throw new Error('Please sign in to continue this conversation.');
      }

      if (!response.ok) {
        throw new Error('The assistant is not reachable. Please try again soon.');
      }

      const payload = await response.json();
      if (payload.sessionId) {
        state.sessionId = payload.sessionId;
        window.localStorage.setItem(SESSION_KEY, payload.sessionId);
      }
      renderAssistantResponse(payload);
    } catch (error) {
      const fallback = error.message || 'Something went wrong. Please try again.';
      pushMessage('assistant', fallback);
      showError(fallback);
    } finally {
      setBusy(false);
    }
  }

  function renderAssistantResponse(payload) {
    const reply = payload.reply || payload.error || 'I did not understand that, but a human can help shortly.';
    pushMessage('assistant', reply);
    renderSuggestions(payload.suggestions || []);
    updateStatus(payload.agentStatus);
  }

  function renderSuggestions(suggestions) {
    if (!suggestionsContainer) {
      return;
    }
    suggestionsContainer.innerHTML = '';
    if (!Array.isArray(suggestions) || suggestions.length === 0) {
      suggestionsContainer.classList.add('hidden');
      return;
    }
    suggestionsContainer.classList.remove('hidden');
    suggestions.forEach((item) => {
      const label = item.label || item.value || item;
      const value = item.value || item.label || item;
      if (!label || !value) {
        return;
      }
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'chat-suggestion';
      button.setAttribute('data-suggestion', value);
      button.textContent = label;
      suggestionsContainer.appendChild(button);
    });
  }

  function updateStatus(agentStatus) {
    if (!status) {
      return;
    }
    const normalized = agentStatus || 'online';
    status.textContent = `Assistant is ${normalized}`;
    status.dataset.state = normalized;
  }

  function setBusy(isBusy) {
    state.busy = isBusy;
    form.classList.toggle('opacity-50', isBusy);
    input.disabled = isBusy;
    if (isBusy) {
      status?.classList.add('chat-status-busy');
    } else {
      status?.classList.remove('chat-status-busy');
    }
  }

  function escapeHtml(value) {
    if (!value) {
      return '';
    }
    return value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  function generateSessionId() {
    if (window.crypto && typeof window.crypto.randomUUID === 'function') {
      return window.crypto.randomUUID();
    }
    return `chat-${Date.now()}-${Math.random().toString(16).slice(2)}`;
  }
})();

