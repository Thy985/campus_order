import { post, del, get } from '@/lib/request';

export interface UploadResponse {
  fileUrl: string;
  fileName: string;
  fileSize: number;
  fileSizeFormatted: string;
  fileType: number;
}

export const uploadFile = (file: File, fileType: number = 1): Promise<UploadResponse> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('fileType', String(fileType));
  
  return post('/api/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const uploadFiles = (files: File[], fileType: number = 1): Promise<UploadResponse[]> => {
  const formData = new FormData();
  files.forEach(file => {
    formData.append('files', file);
  });
  formData.append('fileType', String(fileType));
  
  return post('/api/file/upload/batch', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const deleteFile = (fileId: number): Promise<void> => {
  return del(`/api/file/${fileId}`);
};

export const getFileInfo = (fileId: number): Promise<UploadResponse> => {
  return get(`/api/file/${fileId}`);
};

export const getUploadConfig = (): Promise<Record<string, unknown>> => {
  return get('/api/file/config');
};

export default {
  uploadFile,
  uploadFiles,
  deleteFile,
  getFileInfo,
  getUploadConfig,
};
