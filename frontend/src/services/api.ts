const API_BASE_URL = 'http://localhost:8080';

export interface User {
  id: string;
  email: string;
}

export interface Project {
  id: string;
  name: string;
  createdAt: string;
}

export interface Deployment {
  id: string;
  projectId: string;
  sourceType: 'ZIP' | 'GITHUB';
  projectType: 'NODE' | 'JAVA';
  repoUrl: string;
  status: 'QUEUED' | 'CLONING' | 'BUILDING' | 'UPLOADING' | 'COMPLETED' | 'FAILED';
  errorMessage?: string | null;
  createdAt: string;
}

// Token helper methods
export const getToken = (): string | null => localStorage.getItem('forge_token');
export const setToken = (token: string): void => localStorage.setItem('forge_token', token);
export const removeToken = (): void => localStorage.removeItem('forge_token');

export const decodeToken = (token: string): User | null => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    const payload = JSON.parse(jsonPayload);
    return {
      id: payload.userId || payload.sub,
      email: payload.sub,
    };
  } catch (e) {
    console.error('Error decoding token', e);
    return null;
  }
};

async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getToken();
  
  const headers = new Headers(options.headers || {});
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMessage = 'An error occurred';
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch {
      errorMessage = response.statusText || errorMessage;
    }
    throw new Error(errorMessage);
  }

  // Handle empty responses
  if (response.status === 204) {
    return {} as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  // Auth API
  register: (email: string, password: string) =>
    request<{ email: string }>('/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    }),

  login: async (email: string, password: string) => {
    const response = await request<{ token: string }>('/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });
    if (response.token) {
      setToken(response.token);
    }
    return response;
  },

  logout: () => {
    removeToken();
  },

  getGitHubAuthUrl: () => `${API_BASE_URL}/oauth2/authorization/github`,

  // Projects API
  getProjects: () => request<Project[]>('/api/v1/projects'),

  getProject: (id: string) => request<Project>(`/api/v1/projects/${id}`),

  createProject: (name: string) =>
    request<Project>('/api/v1/projects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name }),
    }),

  // Deployments API
  getDeployment: (id: string) => request<Deployment>(`/api/v1/deployments/${id}`),

  createDeployment: async (
    projectId: string,
    projectType: 'NODE' | 'JAVA',
    file: File,
    buildCommand?: string,
    outputDirectory?: string
  ) => {
    const formData = new FormData();
    formData.append('projectId', projectId);
    formData.append('sourceType', 'ZIP');
    formData.append('projectType', projectType);
    formData.append('file', file);
    
    if (buildCommand && buildCommand.trim() !== '') {
      formData.append('buildCommand', buildCommand);
    }
    if (outputDirectory && outputDirectory.trim() !== '') {
      formData.append('outputDirectory', outputDirectory);
    }

    const token = getToken();
    const headers = new Headers();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/deployments`, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!response.ok) {
      let errorMessage = 'Deployment creation failed';
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorMessage;
      } catch {
        errorMessage = response.statusText || errorMessage;
      }
      throw new Error(errorMessage);
    }

    return response.json() as Promise<Deployment>;
  },
};
